package hr.java.application.routeplanner.interfaces;

import com.sun.jna.Library;

public interface WinRegLib extends Library {
    void Set(char name, int value);
    int Get(char name);
}
