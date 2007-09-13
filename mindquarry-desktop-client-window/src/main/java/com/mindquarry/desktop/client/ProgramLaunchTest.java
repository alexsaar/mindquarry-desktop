package com.mindquarry.desktop.client;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.program.Program;

public class ProgramLaunchTest {
    public static void main(String[] args) throws IOException {
        File temp = File.createTempFile("prefix", "suffix.txt");
        Program.launch(temp.getParent());
    }
}