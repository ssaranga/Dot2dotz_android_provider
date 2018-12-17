package com.dot2dotz.provider.Utilities;

public class MySingleton
{
    private static MySingleton _instance;

    private MySingleton()
    {

    }

    public static MySingleton getInstance()
    {
        if (_instance == null)
        {
            _instance = new MySingleton();
        }
        return _instance;
    }
}