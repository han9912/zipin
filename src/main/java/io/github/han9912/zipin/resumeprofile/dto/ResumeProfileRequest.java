package io.github.han9912.zipin.resumeprofile.dto;

import java.util.List;
import java.util.Map;

public class ResumeProfileRequest {
    public String title;
    public List<Map<String, String>> education;
    public List<Map<String, String>> experience;
    public List<String> skills;

}
