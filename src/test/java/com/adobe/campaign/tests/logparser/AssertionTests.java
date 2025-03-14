/**
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.adobe.campaign.tests.logparser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertThrows;

import java.util.Arrays;
import org.hamcrest.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.adobe.campaign.tests.logparser.LogData;
import com.adobe.campaign.tests.logparser.GenericEntry;
import com.adobe.campaign.tests.logparser.ParseDefinition;
import com.adobe.campaign.tests.logparser.ParseDefinitionEntry;
import com.adobe.campaign.tests.logparser.exceptions.StringParseException;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(LogDataFactory.class)
public class AssertionTests extends PowerMockTestCase {

    /**
     * Testing that we correctly create a cube
     *
     * Author : gandomi
     *
     */
    @Test
    public void testSimpleAssertion() {

        ParseDefinition l_definition = new ParseDefinition("tmp");
        final ParseDefinitionEntry l_parseDefinitionEntryKey = new ParseDefinitionEntry("AAZ");
        l_definition.addEntry(l_parseDefinitionEntryKey);
        l_definition.addEntry(new ParseDefinitionEntry("ZZZ"));
        l_definition.addEntry(new ParseDefinitionEntry("BAU"));
        l_definition.addEntry(new ParseDefinitionEntry("DAT"));
        l_definition.defineKeys(l_parseDefinitionEntryKey);

        GenericEntry l_inputData = new GenericEntry(l_definition);
        l_inputData.fetchValueMap().put("AAZ", "12");
        l_inputData.fetchValueMap().put("ZZZ", "14");
        l_inputData.fetchValueMap().put("BAU", "13");
        l_inputData.fetchValueMap().put("DAT", "AA");

        GenericEntry l_inputData2 = new GenericEntry(l_definition);
        l_inputData2.fetchValueMap().put("AAZ", "112");
        l_inputData2.fetchValueMap().put("ZZZ", "114");
        l_inputData2.fetchValueMap().put("BAU", "113");
        l_inputData2.fetchValueMap().put("DAT", "AAA");

        LogData<GenericEntry> l_cubeData = new LogData<>(l_inputData);
        l_cubeData.addEntry(l_inputData2);

        AssertLogData.assertLogContains(l_cubeData, l_parseDefinitionEntryKey, "112");
        AssertLogData.assertLogContains("This should work", l_cubeData, l_parseDefinitionEntryKey, "112");

        AssertLogData.assertLogContains("This should also work", l_cubeData, "AAZ", "112");

        Assert.assertThrows(AssertionError.class,
                () -> AssertLogData.assertLogContains(l_cubeData, l_parseDefinitionEntryKey, "124"));

        try {
            AssertLogData.assertLogContains("But this should not", l_cubeData,
                    l_parseDefinitionEntryKey.getTitle(), "112");
        } catch (AssertionError ae) {
            assertThat("The comment should contain the passed string", ae.getMessage(), Matchers.containsString("But this should not"));
            assertThat("The comment should contain the name of the parseDefinition title", ae.getMessage(), Matchers.containsString(l_parseDefinitionEntryKey.getTitle()));
        }
    }
    
    
    
    /**
     * Testing a possible usecase. This test is a copy of the test {@link LogDataTest#testLogDataFactory()}
     *
     * Author : gandomi
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws StringParseException
     *
     */
    @Test
    public void testLogDataFactory()
            throws InstantiationException, IllegalAccessException, StringParseException {

        //Create a parse definition

        //Create a parse definition
        ParseDefinitionEntry l_apiDefinition = new ParseDefinitionEntry();

        l_apiDefinition.setTitle("path");
        l_apiDefinition.setStart("HEADER ACTION ");
        l_apiDefinition.setEnd("#");

        ParseDefinitionEntry l_verbDefinition = new ParseDefinitionEntry();

        l_verbDefinition.setTitle("verb");
        l_verbDefinition.setStart("#");
        l_verbDefinition.setEnd(null);

        ParseDefinition l_pDefinition = new ParseDefinition("ACC Coverage");
        l_pDefinition.setDefinitionEntries(Arrays.asList(l_apiDefinition, l_verbDefinition));

        final String apacheLogFile = "src/test/resources/logTests/acc/acc_integro_jenkins_log_exerpt.txt";

        LogData<GenericEntry> l_logData = LogDataFactory.generateLogData(Arrays.asList(apacheLogFile),
                l_pDefinition);

        assertThat(l_logData, is(notNullValue()));
        assertThat("We should have the correct nr of entries", l_logData.getEntries().size(), is(equalTo(5)));

        assertThat("We should have the key for nms:delivery#PrepareFromId",
                l_logData.getEntries().containsKey("nms:delivery#PrepareFromId"));

        AssertLogData.assertLogContains("We should have gound the entry PrepareFromId", Arrays.asList(apacheLogFile), l_pDefinition, "verb", "PrepareFromId");
        AssertLogData.assertLogContains(Arrays.asList(apacheLogFile), l_pDefinition, "verb", "PrepareFromId");
    }
    
    
    /**
     * Testing a possible usecase. This test is a copy of the test {@link LogDataTest#testLogDataFactory()}. Testing Exception 
     *
     * Author : gandomi
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws StringParseException
     *
     */
    @Test
    public void testLogDataFactory_NegativeExceptionThrown()
            throws InstantiationException, IllegalAccessException, StringParseException {
        PowerMockito.mockStatic(LogDataFactory.class);        

        //Create a parse definition
        ParseDefinitionEntry l_apiDefinition = new ParseDefinitionEntry();

        l_apiDefinition.setTitle("path");
        l_apiDefinition.setStart("HEADER ACTION ");
        l_apiDefinition.setEnd("#");

        ParseDefinitionEntry l_verbDefinition = new ParseDefinitionEntry();

        l_verbDefinition.setTitle("verb");
        l_verbDefinition.setStart("#");
        l_verbDefinition.setEnd(null);

        ParseDefinition l_pDefinition = new ParseDefinition("ACC Coverage");
        l_pDefinition.setDefinitionEntries(Arrays.asList(l_apiDefinition, l_verbDefinition));

        final String apacheLogFile = "src/test/resources/logTests/acc/acc_integro_jenkins_log_exerpt.txt";
        
        PowerMockito.when(LogDataFactory.generateLogData(Arrays.asList(apacheLogFile), l_pDefinition)).thenThrow( new InstantiationException("Duuh"));
        
        assertThrows(AssertionError.class, () -> AssertLogData.assertLogContains("We should have found the entry PrepareFromId", Arrays.asList(apacheLogFile), l_pDefinition, "verb", "PrepareFromId")); 
       
        assertThrows(AssertionError.class, () -> AssertLogData.assertLogContains(Arrays.asList(apacheLogFile), l_pDefinition, "verb", "PrepareFromId"));
    }
    
    
    @Test
    public void testLogDataFactory_NegativeInstatiation() {
        assertThrows(IllegalStateException.class, () -> new AssertLogData());
    }

}
