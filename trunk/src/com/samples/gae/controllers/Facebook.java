package com.samples.gae.controllers;

import com.zipeg.gae.*;

public class Facebook extends Context {

    public boolean tab; // fbapp?tab=true
    public boolean admin; // fbapp?tab=true&admin=true for Page Tab Admins

    public void fbApp() {
        appendToHead("<style type=\"text/css\">" +
                "html,body{margin:0;padding:0;border:0;background-color:#FF0000;" +
                "height: 100%;min-height: 100%;}" +
                "</style>");
    }

}
