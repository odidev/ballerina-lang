/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.cmd;

import com.moandjiezana.toml.Toml;
import io.ballerina.projects.utils.FileUtils;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;
import org.ballerinalang.toml.model.Module;
import org.ballerinalang.tool.BLauncherCmd;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.packerina.cmd.Constants.ADD_COMMAND;

/**
 * New command for adding a new module.
 */
@CommandLine.Command(name = ADD_COMMAND, description = "Add a new module to Ballerina project")
public class AddCommand implements BLauncherCmd {

    private Path userDir;
    private PrintStream errStream;
    private Path homeCache;

    @CommandLine.Parameters
    private List<String> argList;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--template", "-t"})
    private String template = "main";

    @CommandLine.Option(names = {"--list", "-l"})
    private boolean list = false;

    public AddCommand() {
        userDir = Paths.get(System.getProperty(ProjectConstants.USER_DIR));
        errStream = System.err;
        homeCache = RepoUtils.createAndGetHomeReposPath();
        CommandUtil.initJarFs();
    }

    public AddCommand(Path userDir, PrintStream errStream) {
        this(userDir, errStream, RepoUtils.createAndGetHomeReposPath());
    }

    public AddCommand(Path userDir, PrintStream errStream, Path homeCache) {
        this.userDir = userDir;
        this.errStream = errStream;
        CommandUtil.initJarFs();
        this.homeCache = homeCache;
    }

    @Override
    public void execute() {
        // If help flag is given print the help message.
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(ADD_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        if (list) {
            errStream.println("Available templates:");
            for (String template : CommandUtil.getTemplates()) {
                errStream.println("    - " + template);
            }
            // Get templates from balos
            for (String template : getBaloTemplates()) {
                errStream.println("    - " + template);
            }
            return;
        }

        // Check if inside a project repo
        Path projectPath = ProjectUtils.findProjectRoot(userDir);
        if (null == projectPath) {
            CommandUtil.printError(errStream,
                    "not a ballerina project (or any parent up to mount point)\n" +
                            "You should run this command inside a ballerina project", null, false);
            return;
        }

        // Check if an argument is provided
        if (null == argList) {
            CommandUtil.printError(errStream,
                    "The following required arguments were not provided:\n" +
                            "    <module-name>",
                    "ballerina add <module-name> [-t|--template <template-name>]",
                    true);
            return;
        }

        // Check if more then one argument is provided
        if (!(1 == argList.size())) {
            CommandUtil.printError(errStream,
                    "too many arguments.",
                    "ballerina add <project-name>",
                    true);
            return;
        }

        // Check if the provided arg a valid module name
        String moduleName = argList.get(0);
        boolean matches = ProjectUtils.validatePkgName(moduleName);
        if (!matches) {
            CommandUtil.printError(errStream,
                    "Invalid module name : '" + moduleName + "' :\n" +
                            "Module name can only contain alphanumerics, underscores and periods " +
                            "and the maximum length is 256 characters",
                    null,
                    false);
            return;
        }

        // Check if the module already exists
        if (ProjectUtils.isModuleExist(projectPath, moduleName)) {
            CommandUtil.printError(errStream,
                    "A module already exists with the given name : '" + moduleName + "' :\n" +
                            "Existing module path "
                            + projectPath.resolve(ProjectConstants.MODULES_DIR_NAME).resolve(moduleName),
                    null,
                    false);
            return;
        }

        // Check if the template exists
        if (!CommandUtil.getTemplates().contains(template) && findBaloTemplate(template) == null) {
            CommandUtil.printError(errStream,
                    "Template not found, use `ballerina add --list` to view available templates.",
                    null,
                    false);
            return;
        }

        try {
            Path moduleDirPath = projectPath.resolve(ProjectConstants.MODULES_DIR_NAME);
            if (!Files.exists(moduleDirPath)) {
                Files.createDirectory(moduleDirPath);
            }
            createModule(projectPath, moduleName, template);
        } catch (ModuleCreateException | IOException e) {
            CommandUtil.printError(errStream,
                    "Error occurred while creating module : " + e.getMessage(),
                    null,
                    false);
            return;
        }

        errStream.println("Added new ballerina module at '" + userDir.relativize(projectPath
                .resolve(ProjectConstants.MODULES_DIR_NAME)
                .resolve(moduleName)) + "'");
    }

    @Override
    public String getName() {
        return ADD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("add a new ballerina module");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina add <module-name> [-t|--template <template-name>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private void createModule(Path projectPath, String moduleName, String template)
            throws ModuleCreateException {
        Path modulePath = projectPath.resolve(ProjectConstants.MODULES_DIR_NAME).resolve(moduleName);
        try {
            Files.createDirectories(modulePath);

            // We will be creating following in the module directory
            // - modules/
            // -- mymodule/
            // --- Module.md      <- module level documentation
            // --- main.bal       <- Contains default main method.
            // --- resources/     <- resources for the module (available at runtime)
            // --- tests/         <- tests for this module (e.g. unit tests)
            // ---- main_test.bal  <- test file for main
            // ---- resources/    <- resources for these tests
            if (CommandUtil.getTemplates().contains(template)) {
                CommandUtil.applyTemplate(modulePath, template);
            } else {
                applyBaloTemplate(modulePath, template);
            }

        } catch (AccessDeniedException e) {
            throw new ModuleCreateException("Insufficient Permission");
        } catch (IOException | URISyntaxException e) {
            throw new ModuleCreateException(e.getMessage());
        }
    }

    private void applyBaloTemplate(Path modulePath, String template) {
        // find all balos matching org and module name.
        Path baloTemplate = findBaloTemplate(template);
        if (baloTemplate != null) {
            String moduleName = getModuleName(baloTemplate);

            URI zipURI = URI.create("jar:" + baloTemplate.toUri().toString());
            try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
                // Copy sources
                Path srcDir = zipfs.getPath("/modules").resolve(moduleName);
                // We do a string comparison to be efficient.
                Files.walkFileTree(srcDir, new FileUtils.Copy(srcDir, modulePath));

                // Copy resources
                Path resourcesDir = zipfs.getPath("/" + ProjectConstants.RESOURCE_DIR_NAME);
                Path moduleResources = modulePath.resolve(ProjectConstants.RESOURCE_DIR_NAME);
                Files.createDirectories(moduleResources);
                // We do a string comparison to be efficient.
                Files.walkFileTree(resourcesDir, new FileUtils.Copy(resourcesDir, moduleResources));
                // Copy Module.md
                Path moduleMd = zipfs.getPath("/docs").resolve(ProjectConstants.MODULE_MD_FILE_NAME);
                Path toModuleMd = modulePath.resolve(ProjectConstants.MODULE_MD_FILE_NAME);
                Files.copy(moduleMd, toModuleMd, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                CommandUtil.printError(errStream,
                        "Error while applying template : " + e.getMessage(),
                        null,
                        false);
                Runtime.getRuntime().exit(1);
            }
        }
    }

    private String getModuleName(Path baloTemplate) {
        Path baloName = baloTemplate.getFileName();
        if (baloName != null) {
            String fileName = baloName.toString();
            return fileName.split("-")[0];
        }
        return "";
    }

    private Path findBaloTemplate(String template) {
        // Split the template in to parts
        String[] orgSplit = template.split("/");
        String orgName = orgSplit[0].trim();
        String moduleName = "";
        String version = "*";
        String modulePart = (orgSplit.length > 1) ? orgSplit[1] : "";
        String[] moduleSplit = modulePart.split(":");
        moduleName = moduleSplit[0].trim();
        version = (moduleSplit.length > 1) ? moduleSplit[1].trim() : version;

        String baloGlob = "glob:**/" + orgName + "/" + moduleName + "/" + version + "/*.balo";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(baloGlob);
        Path baloCache = this.homeCache.resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
        // Iterate directories
        try (Stream<Path> walk = Files.walk(baloCache)) {

            List<Path> baloList = walk
                    .filter(pathMatcher::matches)
                    .collect(Collectors.toList());

            Collections.sort(baloList);
            // get the latest
            if (baloList.size() > 0) {
                return baloList.get(baloList.size() - 1);
            } else {
                return null;
            }
        } catch (IOException e) {
            CommandUtil.printError(errStream,
                    "Unable to read home cache",
                    null,
                    false);
            Runtime.getRuntime().exit(1);
        }

        return homeCache.resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
    }

    /**
     * Iterate home cache and search for template balos.
     *
     * @return list of templates
     */
    private List<String> getBaloTemplates() {
        List<String> templates = new ArrayList<>();
        // get the path to home cache
        Path baloCache = this.homeCache.resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.balo");
        // Iterate directories
        try (Stream<Path> walk = Files.walk(baloCache)) {

            List<Path> baloList = walk
                    .filter(pathMatcher::matches)
                    .filter(this::isTemplateBalo)
                    .collect(Collectors.toList());

            // Convert the balo list to string list.
            templates = baloList.stream()
                    .map(this::getModuleToml)
                    .filter(o -> o != null)
                    .map(m -> {
                        return m.getModule_organization() + "/" + m.getModule_name();
                    })
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            CommandUtil.printError(errStream,
                    "Unable to read home cache",
                    null,
                    false);
            Runtime.getRuntime().exit(1);
        }
        // filter template modules
        return templates;
    }

    private Module getModuleToml(Path baloPath) {
        URI zipURI = URI.create("jar:" + baloPath.toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
            // We do a string comparison to be efficient.
            String content = new String(Files.readAllBytes(metaDataToml), StandardCharsets.UTF_8);
            Toml toml = new Toml().read(content);
            return toml.to(Module.class);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isTemplateBalo(Path baloPath) {
        URI zipURI = URI.create("jar:" + baloPath.toUri().toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path metaDataToml = zipfs.getPath("metadata", "MODULE.toml");
            // We do a string comparison to be efficient.
            return new String(Files.readAllBytes(metaDataToml), StandardCharsets.UTF_8)
                    .contains("template = \"true\"");
        } catch (IOException e) {
            // we simply ignore the balo file
        }
        return false;
    }

    static class ModuleCreateException extends Exception {
        public ModuleCreateException(String message) {
            super(message);
        }
    }
}
