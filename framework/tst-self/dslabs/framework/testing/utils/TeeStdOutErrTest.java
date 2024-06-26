/*
 * Copyright (c) 2023 Ellis Michael
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package dslabs.framework.testing.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TeeStdOutErrTest {
    @Before
    @After
    public void clearTees() {
        try {
            TeeStdOutErr.clearTees();
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void testStdOut() {
        TeeStdOutErr.installTees();
        System.out.println("foo");
        var result = TeeStdOutErr.clearTees();
        assertEquals(result.stdOut(), "foo\n");
        assertEquals(result.stdErr(), "");
    }

    @Test
    public void testStdErr() {
        TeeStdOutErr.installTees();
        System.err.println("foo");
        var result = TeeStdOutErr.clearTees();
        assertEquals(result.stdOut(), "");
        assertEquals(result.stdErr(), "foo\n");
    }

    @Test(expected = AssertionError.class)
    public void testDoubleTee() {
        TeeStdOutErr.installTees();
        TeeStdOutErr.installTees();
    }


    @Test(expected = AssertionError.class)
    public void testClearBeforeInstall() {
        TeeStdOutErr.clearTees();
    }
}
