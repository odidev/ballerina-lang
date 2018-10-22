/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of type check expressions.
 */
public class TypeCheckExprTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
         result = BCompileUtil.compile("test-src/expressions/binaryoperations/type-check-expr.bal");
    }

    @Test
    public void testTypeCheckExprNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/expressions/binaryoperations/type-check-expr-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 32);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 19, 9);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'int' will not be matched to 'float'", 28,
                9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 37, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'int' will not be matched to 'string|float'", 46, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 55, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 64, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'int|string' will not be matched to 'boolean|float'", 73, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 91, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 93, 16);
        BAssertUtil.validateError(negativeResult, i++, "unknown type 'C'", 98, 14);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 118, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 120, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 131, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 131, 32);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: 'int[]' will not be matched to 'float[]'",
                132, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 133, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 134, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 135, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 141, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: '(int,string)' will not be matched to '(float,boolean)'", 142, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 143, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 144, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 150, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 151, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 157, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 158, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 159, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: 'map<string>' will not be matched to 'json'", 160, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 161, 18);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 178, 16);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 188, 13);
        BAssertUtil.validateError(negativeResult, i++,
                "unnecessary condition: expression will always evaluate to 'true'", 188, 23);
    }

    @Test
    public void testValueTypeInUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testValueTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test
    public void testUnionTypeInUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "numeric");
    }

    @Test
    public void testSimpleRecordTypes_1() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is A1");
    }

    @Test
    public void testSimpleRecordTypes_2() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSimpleRecordTypes_3() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleRecordTypes_3");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testNestedRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedRecordTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSealedRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testSealedRecordTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testNestedTypeCheck() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedTypeCheck");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "boolean");
        Assert.assertEquals(returns[1].stringValue(), "int");
        Assert.assertEquals(returns[2].stringValue(), "string");
    }

    @Test
    public void testTypeInAny() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeInAny");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string value: This is working");
    }

    @Test
    public void testNilType() {
        BValue[] returns = BRunUtil.invoke(result, "testNilType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "nil");
    }

    @Test
    public void testJSONTypeCheck() {
        BValue[] returns = BRunUtil.invoke(result, "testJSONTypeCheck");
        Assert.assertEquals(returns.length, 7);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertSame(returns[5].getClass(), BString.class);
        Assert.assertSame(returns[6].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "json int");
        Assert.assertEquals(returns[1].stringValue(), "json float");
        Assert.assertEquals(returns[2].stringValue(), "json string: hello");
        Assert.assertEquals(returns[3].stringValue(), "json boolean");
        Assert.assertEquals(returns[4].stringValue(), "json array");
        Assert.assertEquals(returns[5].stringValue(), "json object");
        Assert.assertEquals(returns[6].stringValue(), "json null");
    }

    @Test
    public void testRecordsWithFunctionType_1() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordsWithFunctionType_1");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is not a man");
        Assert.assertEquals(returns[1].stringValue(), "Human: Piyal");
    }

    @Test
    public void testRecordsWithFunctionType_2() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordsWithFunctionType_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Man: Piyal");
        Assert.assertEquals(returns[1].stringValue(), "Human: Piyal");
    }

    @Test
    public void testObjectWithUnorderedFields() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithUnorderedFields");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "I am a person in order: John");
        Assert.assertEquals(returns[1].stringValue(), "I am a person not in order: John");
        Assert.assertEquals(returns[2].stringValue(), "I am a person in order: Doe");
        Assert.assertEquals(returns[3].stringValue(), "I am a person not in order: Doe");
    }

    @Test
    public void testObjectWithSameMembersButDifferentAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithSameMembersButDifferentAlias");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "I am same as person: John");
        Assert.assertEquals(returns[1].stringValue(), "I am a person: John");
        Assert.assertEquals(returns[2].stringValue(), "I am same as person: Doe");
        Assert.assertEquals(returns[3].stringValue(), "I am a person: Doe");
    }

    @Test
    public void testSimpleArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleArrays");
        Assert.assertEquals(returns.length, 5);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testRecordArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordArrays");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testSimpleTuples() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleTuples");
        Assert.assertEquals(returns.length, 5);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testTupleWithAssignableTypes_1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleWithAssignableTypes_1");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testTupleWithAssignableTypes_2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleWithAssignableTypes_2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleUnconstrainedMap_1() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleUnconstrainedMap_1");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSimpleUnconstrainedMap_2() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleUnconstrainedMap_2");
        Assert.assertEquals(returns.length, 5);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testSimpleConstrainedMap() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleConstrainedMap");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }
}
