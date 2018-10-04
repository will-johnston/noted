package com.cs407.noted;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RemoveNotesUnitTest {
    @Mock
    MainActivity mainActivity;

    @Mock
    ListAdapter listAdapter;

    private String doc_name_1 = "test document1";
    private String doc_name_2 = "test document2";
    private String doc_name_3 = "test document3";

    private File root = new File("root", null, "root",
            null, null, "DOCUMENT", null);
    private File file1 = new File("test1", "root", doc_name_1,
            null, null, "DOCUMENT", null);
    private File file2 = new File("test2", "root", doc_name_2,
            null, null, "DOCUMENT", null);
    private File file3 = new File("test3", "root", doc_name_3,
            null, null, "DOCUMENT", null);
    private List<File> list1 = new ArrayList(Arrays.asList(new File[]{file1, file2, file3}));
    private List<File> list2 = new ArrayList(Arrays.asList(new File[]{file1, file2}));
    private List<File> list3 = new ArrayList(Arrays.asList(new File[]{file1, file3}));
    private List<File> list4 = new ArrayList(Arrays.asList(new File[]{file3, file2}));
    private List<File> list5 = new ArrayList(Arrays.asList(new File[]{file3}));




    @Test
    public void test_delete_note_1() {
        doAnswer(new Answer() {
            @Override
            public List<File> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return listAdapter.getItemList();
            }
        }).when(mainActivity).removeFile((File) anyObject(), (File) anyObject());

        when(listAdapter.getItemList()).thenReturn(list2);

        mainActivity.removeFile(file3, root);
        assert listAdapter.getItemList().size() == 2;
        assert !listAdapter.getItemList().contains(file3);
    }

    @Test
    public void test_delete_note_2() {
        doAnswer(new Answer() {
            @Override
            public List<File> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return listAdapter.getItemList();
            }
        }).when(mainActivity).removeFile((File) anyObject(), (File) anyObject());

        when(listAdapter.getItemList()).thenReturn(list3);

        mainActivity.removeFile(file2, root);
        assert listAdapter.getItemList().size() == 2;
        assert !listAdapter.getItemList().contains(file2);
    }

    @Test
    public void test_delete_note_3() {
        doAnswer(new Answer() {
            @Override
            public List<File> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return listAdapter.getItemList();
            }
        }).when(mainActivity).removeFile((File) anyObject(), (File) anyObject());

        when(listAdapter.getItemList()).thenReturn(list4);

        mainActivity.removeFile(file1, root);
        assert listAdapter.getItemList().size() == 2;
        assert !listAdapter.getItemList().contains(file1);
    }


    @Test
    public void test_delete_note_4() {
        doAnswer(new Answer() {
            @Override
            public List<File> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return listAdapter.getItemList();
            }
        }).when(mainActivity).removeFile((File) anyObject(), (File) anyObject());

        when(listAdapter.getItemList()).thenReturn(list5);

        mainActivity.removeFile(file1, root);
        mainActivity.removeFile(file2, root);
        assert listAdapter.getItemList().size() == 1;
        assert listAdapter.getItemList().contains(file3);
    }
}
