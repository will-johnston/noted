package com.cs407.noted;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import io.github.mthli.knife.KnifeText;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.intThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class EditDocumentUnitTest {

    @Mock
    KnifeText knifeText;

    @Mock
    DocumentActivity documentActivity;

    private String text = "this is a normal string";
    private String format_text = "<i>this is italicized</i>";

    private String current_text = "I have written up to here";  // 25 chars
    private String added_text_after = "I have written up to here and added this text";  // 20 extra characters
    private String added_text_before = "I haven't written up to here";  // 3 extra characters
    private String deleted_text_after = "I have written up to";
    private String deleted_text_before = "I written up to here";

    private int start_pos_1 = 25;
    private int start_pos_2 = 20;
    private int start_pos_3 = 0;
    private int start_pos_4 = -1;

    private int end_pos_1 = 25;
    private int end_pos_2 = 20;
    private int end_pos_3 = 0;


    // mock adding text to knife text on activity
    @Test
    public void test_add_text_1() {
        doAnswer(new Answer() {
             @Override
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 return documentActivity.getCurrentHtml();
             }
        }).when(documentActivity).addChangeToDatabase();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return text;
            }
        }).when(documentActivity).getCurrentHtml();

        documentActivity.addChangeToDatabase();
        String change = documentActivity.getCurrentHtml();
        assert change.equals(text);
    }

    @Test
    public void test_add_text_2() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return documentActivity.getCurrentHtml();
            }
        }).when(documentActivity).addChangeToDatabase();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return format_text;
            }
        }).when(documentActivity).getCurrentHtml();

        documentActivity.addChangeToDatabase();
        String change = documentActivity.getCurrentHtml();
        assert change.equals(format_text);
    }

    @Test
    public void test_cursor_pos_add_text_after_cursor() {
        // updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed)
        int diff = added_text_after.length() - current_text.length();
        int len1 = current_text.length();
        int len2 = added_text_after.length();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                if ((int) args[3] > (int) args[2]) {
                    // changed length > current length, return original start
                    return new int[]{(int) args[0], (int) args[1]};
                }
                return null;
            }
        }).when(documentActivity).updateStartEnd(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString());

        int[] ret = documentActivity.updateStartEnd(start_pos_1, end_pos_1, len1, len2, diff,
                current_text, added_text_after);

        assert(ret[0] == 25 && ret[1] == 25);
    }

    @Test
    public void test_cursor_pos_add_text_after_cursor_2() {
        // updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed)
        int diff = added_text_after.length() - current_text.length();
        int len1 = current_text.length();
        int len2 = added_text_after.length();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                if ((int) args[3] > (int) args[2]) {
                    // changed length > current length, return original start
                    return new int[]{(int) args[0], (int) args[1]};
                }
                return null;
            }
        }).when(documentActivity).updateStartEnd(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString());

        int[] ret = documentActivity.updateStartEnd(start_pos_3, end_pos_3, len1, len2, diff,
                current_text, added_text_after);

        assert(ret[0] == 0 && ret[1] == 0);
    }

    @Test
    public void test_cursor_pos_add_text_before_cursor() {
        // updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed)
        int diff = added_text_before.length() - current_text.length();
        int len1 = current_text.length();
        int len2 = added_text_before.length();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                if ((int) args[3] > (int) args[2]) {
                    if (!((String)args[6]).substring(0, (int) args[2]).equals((String) args[5])) {
                        // if the new changes have edits before the current cursor positions,
                        // add diff to start and end positions
                        return new int[]{(int) args[0] + (int) args[4], (int) args[1] + (int) args[4]};
                    }
                }
                return null;
            }
        }).when(documentActivity).updateStartEnd(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString());

        int[] ret = documentActivity.updateStartEnd(start_pos_1, end_pos_1, len1, len2, diff,
                current_text, added_text_before);

        assert(ret[0] == 28 && ret[1] == 28);
    }

    @Test
    public void test_cursor_pos_delete_text_after_cursor() {
        // updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed)
        final int diff = Math.abs(deleted_text_after.length() - current_text.length());
        int len1 = current_text.length();
        int len2 = deleted_text_after.length();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                if ((int) args[3] < (int) args[2]) {
                    if ((int) args[1] > (int) args[3]) {
                        // if the new changes have edits before the current cursor positions,
                        // subtract diff to start and end positions
                         return new int[]{(int) args[0]- diff, (int) args[1] - diff};
                    }
                }
                return null;
            }
        }).when(documentActivity).updateStartEnd(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString());

        int[] ret = documentActivity.updateStartEnd(start_pos_1, end_pos_1, len1, len2, diff,
                current_text, deleted_text_after);

        assert(ret[0] == 20 && ret[1] == 20);
    }

    @Test
    public void test_cursor_pos_delete_text_before_cursor() {
        // updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed)
        final int diff = Math.abs(deleted_text_before.length() - current_text.length());
        int len1 = current_text.length();
        int len2 = deleted_text_before.length();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                if ((int) args[3] < (int) args[2]) {
                    if ((int) args[1] > (int) args[3]) {
                        // if the new changes have edits before the current cursor positions,
                        // subtract diff to start and end positions
                        return new int[]{(int) args[0] - diff, (int) args[1] - diff};
                    }
                }
                return null;
            }
        }).when(documentActivity).updateStartEnd(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString());

        int[] ret = documentActivity.updateStartEnd(start_pos_1, end_pos_1, len1, len2, diff,
                current_text, deleted_text_before);

        assert(ret[0] == 20 && ret[1] == 20);
    }

    @Test
    public void test_cursor_pos_delete_text_before_cursor_2() {
        // updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed)
        final int diff = Math.abs(deleted_text_before.length() - current_text.length());
        int len1 = current_text.length();
        int len2 = deleted_text_before.length();


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                if ((int) args[3] < (int) args[2]) {
                    int end = (int) args[1];
                    int changedLen = (int) args[3];
                    String current = (String) args[5];
                    String changed = (String) args[6];
                    String current_sub = current.substring(0, end);
                    String changed_sub = changed.substring(0, end);
                    if (end <= changedLen && !current_sub.equals(changed_sub)) {
                        // if the new changes have edits before the current cursor positions,
                        // subtract diff to start and end positions
                        return new int[]{(int) args[0] - diff, (int) args[1] - diff};
                    }
                }
                return null;
            }
        }).when(documentActivity).updateStartEnd(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString());

        int[] ret = documentActivity.updateStartEnd(start_pos_2, end_pos_2, len1, len2, diff,
                current_text, deleted_text_before);

        assert(ret[0] == 15 && ret[1] == 15);
    }






}
