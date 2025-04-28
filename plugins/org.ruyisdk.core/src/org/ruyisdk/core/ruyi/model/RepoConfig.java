package org.ruyisdk.core.ruyi.model;

public class RepoConfig {
    private String name;
    private String url;
    private int priority; //0 hightest

    // 带参构造器
    public RepoConfig(String name, String url,int priority) {
        this.name = name;
        this.url = url;
        this.priority = priority;
    }

    // Getter/Setter 方法
    public String getName() { return name; }
    public String getUrl() { return url; }
    public int getPriority() { return priority; }
    public void setUrl(String url) { this.url = url; } 
}
