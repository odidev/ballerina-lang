/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerCache;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Code Action related Utils.
 *
 * @since 1.0.1
 */
public class CodeActionUtil {
    private static final Logger logger = LoggerFactory.getLogger(CodeActionUtil.class);

    private CodeActionUtil() {
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param identifier Document Identifier
     * @param cursorLine Cursor line
     * @param docManager Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static CodeActionNodeType topLevelNodeInLine(LSContext context, TextDocumentIdentifier identifier, int cursorLine,
                                                        WorkspaceDocumentManager docManager) {
        Optional<Path> filePath = CommonUtil.getPathFromURI(identifier.getUri());
        LSCompilerCache.clearAll();
        if (!filePath.isPresent()) {
            return null;
        }

        try {
            BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager,
                                                                         LSCustomErrorStrategy.class, false, false);
            String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangPackage evalPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);

            List<Diagnostic> diagnostics = new ArrayList<>();
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
                diagnostics = ((CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class))
                        .getDiagnostics();
            }
            context.put(CodeActionKeys.DIAGNOSTICS_KEY, CodeActionUtil.toDiagnostics(diagnostics));

            Optional<BLangCompilationUnit> filteredCUnit = evalPkg.compUnits.stream()
                    .filter(cUnit -> cUnit.getPosition().getSource()
                            .cUnitName.replace("/", CommonUtil.FILE_SEPARATOR)
                            .equals(relativeSourcePath))
                    .findAny();

            if (!filteredCUnit.isPresent()) {
                return null;
            }

            for (TopLevelNode topLevelNode : filteredCUnit.get().getTopLevelNodes()) {
                DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition(((BLangNode) topLevelNode).pos);
                if (topLevelNode instanceof BLangService) {
                    if (diagnosticPos.sLine == cursorLine) {
                        return CodeActionNodeType.SERVICE;
                    }
                    if (cursorLine > diagnosticPos.sLine && cursorLine < diagnosticPos.eLine) {
                        // Cursor within the service
                        for (BLangFunction resourceFunction : ((BLangService) topLevelNode).resourceFunctions) {
                            diagnosticPos = CommonUtil.toZeroBasedPosition(resourceFunction.getName().pos);
                            if (diagnosticPos.sLine == cursorLine) {
                                return CodeActionNodeType.RESOURCE;
                            }
                        }
                    }
                }

                if (topLevelNode instanceof BLangImportPackage && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.IMPORTS;
                }

                if (topLevelNode instanceof BLangFunction && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.FUNCTION;
                }

                if (topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangRecordTypeNode
                        && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.RECORD;
                }
                if (topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                    if (diagnosticPos.sLine == cursorLine) {
                        return CodeActionNodeType.OBJECT;
                    }
                    if (cursorLine > diagnosticPos.sLine && cursorLine < diagnosticPos.eLine) {
                        // Cursor within the object
                        for (BLangFunction resourceFunction
                                : ((BLangObjectTypeNode) ((BLangTypeDefinition) topLevelNode).typeNode).functions) {
                            diagnosticPos = CommonUtil.toZeroBasedPosition(resourceFunction.getName().pos);
                            if (diagnosticPos.sLine == cursorLine) {
                                return CodeActionNodeType.OBJECT_FUNCTION;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (CompilationFailedException e) {
            logger.error("Error while compiling the source");
            return null;
        }
    }

    /**
     * Translates ballerina diagnostics into lsp4j diagnostics.
     *
     * @param ballerinaDiags a list of {@link org.ballerinalang.util.diagnostic.Diagnostic}
     * @return a list of {@link Diagnostic}
     */
    public static List<org.eclipse.lsp4j.Diagnostic> toDiagnostics(
            List<org.ballerinalang.util.diagnostic.Diagnostic> ballerinaDiags) {
        List<org.eclipse.lsp4j.Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaDiags.forEach(diagnostic -> {
            org.eclipse.lsp4j.Diagnostic lsDiagnostic = new org.eclipse.lsp4j.Diagnostic();
            lsDiagnostic.setSeverity(DiagnosticSeverity.Error);
            lsDiagnostic.setMessage(diagnostic.getMessage());
            Range r = new Range();

            int startLine = diagnostic.getPosition().getStartLine() - 1; // LSP diagnostics range is 0 based
            int startChar = diagnostic.getPosition().getStartColumn() - 1;
            int endLine = diagnostic.getPosition().getEndLine() - 1;
            int endChar = diagnostic.getPosition().getEndColumn() - 1;

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            r.setStart(new Position(startLine, startChar));
            r.setEnd(new Position(endLine, endChar));
            lsDiagnostic.setRange(r);

            lsDiagnostics.add(lsDiagnostic);
        });

        return lsDiagnostics;
    }
}
