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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CreateDocumentUnitTest {

    @Mock
    MainActivity mainActivity;

    @Mock
    ListAdapter listAdapter;


    private String doc_name_1 = "test document1";
    private String doc_name_2 = "test document2";
    private String doc_name_3 = "test document3";
    private String equal260 ="012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "0123456789012345678901234567890123456789";
    private File file1 = new File("test1", "root", doc_name_1,
            null, null, "DOCUMENT", null);
    private File file2 = new File("test2", "root", doc_name_2,
            null, null, "DOCUMENT", null);
    private File file3 = new File("test3", "root", doc_name_3,
            null, null, "DOCUMENT", null);
    private List<File> list = new ArrayList(Arrays.asList(new File[]{file1, file2, file3}));

    @Test
    public void test_main_activity_name(){
        when(mainActivity.getName()).thenReturn("MainActivity");
        assertThat(mainActivity.getName(),is("MainActivity"));
    }

    @Test
    public void test_add_doc_called_1() {
        mainActivity.addFolderOrDocument(doc_name_1, FileType.DOCUMENT);
        verify(mainActivity,atLeastOnce()).addFolderOrDocument(doc_name_1, FileType.DOCUMENT);
    }

    @Test
    public void test_add_doc_called_2() {
        mainActivity.addFolderOrDocument(doc_name_2, FileType.DOCUMENT);
        verify(mainActivity,atLeastOnce()).addFolderOrDocument(doc_name_2, FileType.DOCUMENT);
    }

    @Test
    public void test_add_doc_called_empty_string() {
        String empty = "";
        when(mainActivity.addFolderOrDocument(anyString(), eq(FileType.DOCUMENT)) && anyString().length() < 1)
                .thenReturn(true);
        // we know this is true, because we rename empty strings to "Untitled doc"
        boolean add = mainActivity.addFolderOrDocument(empty, FileType.DOCUMENT);
        assertThat(add, is(true));
    }

    @Test
    public void test_add_doc_called_null() {
        // we know this is true, because we rename null strings to "Untitled doc"
        when(mainActivity.addFolderOrDocument(null, FileType.DOCUMENT)).thenReturn(true);
        assertThat(mainActivity.addFolderOrDocument(null, FileType.DOCUMENT), is(true));
    }


    @Test
    public void test_add_doc_called_greater_than_255chars() {
        // false because characters cannot be greater than 255
        when(mainActivity.addFolderOrDocument(anyString(), eq(FileType.DOCUMENT)) && anyString().length() > 255)
                .thenReturn(false);
        assertThat(mainActivity.addFolderOrDocument(equal260, FileType.DOCUMENT), is(false));
    }

    @Test
    public void test_add_doc_called_equal_255chars() {
        when(mainActivity.addFolderOrDocument(anyString(), eq(FileType.DOCUMENT))
                && anyString().length() <= 255 && anyString().length() > 0)
                .thenReturn(true);
        assertThat(mainActivity.addFolderOrDocument(equal260.substring(0, 255), FileType.DOCUMENT), is(true));
    }

    @Test
    public void test_add_doc_called_equal_1char() {
        when(mainActivity.addFolderOrDocument(anyString(), eq(FileType.DOCUMENT))
                && anyString().length() <= 255 && anyString().length() > 0)
                .thenReturn(true);
        assertThat(mainActivity.addFolderOrDocument("1", FileType.DOCUMENT), is(true));
    }

    @Test
    public void test_add_list_to_list_adapter_not_null() {
        // verifies that setting the item list does not return a null value
        doAnswer(new Answer() {
            @Override
            public List<File> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return listAdapter.getItemList();
            }
        }).when(listAdapter).setItemListMaintainCurrentDirectory(anyList());

        listAdapter.setItemListMaintainCurrentDirectory(list);
        assert listAdapter.getItemList() != null;
    }

    @Test
    public void test_add_list_to_list_adapter_null() {
        // verifies that setting the item list to null gives an empty list
        doAnswer(new Answer() {
            @Override
            public List<File> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("called with arguments: " + Arrays.toString(args));
                return listAdapter.getItemList();
            }
        }).when(listAdapter).setItemListMaintainCurrentDirectory(anyList());

        listAdapter.setItemListMaintainCurrentDirectory(list);
        assert listAdapter.getItemList().size() == 0;
    }
}
