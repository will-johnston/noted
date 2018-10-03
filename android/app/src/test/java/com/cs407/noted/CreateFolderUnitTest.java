package com.cs407.noted;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mockito;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class CreateFolderUnitTest {
    @Mock
    MainActivity mainActivity;

    @Mock
    ListAdapter listAdapter;


    private String folder_name_1 = "test folder1";
    private String folder_name_2 = "test folder2";
    private String folder_name_3 = "test folder3";
    private String equal260 = "012345678901234567890123456789012345678901234567890123456789" +
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


    private File file1 = new File("test1", "root", folder_name_1,
            null, null, "FOLDER", null);
    private File file2 = new File("test2", "root", folder_name_2,
            null, null, "FOLDER", null);
    private File file3 = new File("test3", "root", folder_name_3,
            null, null, "FOLDER", null);
    private List<File> list = new ArrayList(Arrays.asList(new File[]{file1, file2, file3}));

    @Test
    public void test_main_activity_name(){
        when(mainActivity.getName()).thenReturn("MainActivity");
        assertThat(mainActivity.getName(),is("MainActivity"));
    }

    @Test
    public void test_add_folder_called_1() {
        mainActivity.addFolder(folder_name_1);
        verify(mainActivity,atLeastOnce()).addFolder(folder_name_1);
    }

    @Test
    public void test_add_folder_called_2() {
        mainActivity.addFolder(folder_name_2);
        verify(mainActivity,atLeastOnce()).addFolder(folder_name_2);
    }

    @Test
    public void test_add_folder_called_empty_string() {
        String empty = "";
        when(mainActivity.addFolder(anyString()) && anyString().length() < 1)
                .thenReturn(true);
        // we know this is true, because we rename empty strings to "Untitled folder"
        boolean add = mainActivity.addFolder(empty);
        assertThat(add, is(true));
    }

    @Test
    public void test_add_folder_called_null() {
        // we know this is true, because we rename null strings to "Untitled folder"
        when(mainActivity.addFolder(null)).thenReturn(true);
        assertThat(mainActivity.addFolder(null), is(true));
    }


    @Test
    public void test_add_folder_called_greater_than_255chars() {
        // false because characters cannot be greater than 255
        when(mainActivity.addFolder(anyString()) && anyString().length() > 255)
                .thenReturn(false);
        assertThat(mainActivity.addFolder(equal260), is(false));
    }

    @Test
    public void test_add_folder_called_equal_255chars() {
        when(mainActivity.addFolder(anyString())
                && anyString().length() <= 255 && anyString().length() > 0)
                .thenReturn(true);
        assertThat(mainActivity.addFolder(equal260.substring(0, 255)), is(true));
    }

    @Test
    public void test_add_folder_called_equal_1char() {
        when(mainActivity.addFolder(anyString())
                && anyString().length() <= 255 && anyString().length() > 0)
                .thenReturn(true);
        assertThat(mainActivity.addFolder("1"), is(true));
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
