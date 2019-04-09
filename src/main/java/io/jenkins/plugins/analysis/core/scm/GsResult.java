package io.jenkins.plugins.analysis.core.scm;

import java.io.Serializable;

/**
 * represent a single result
 */
public class GsResult implements Serializable {
    private Integer age;
    private Integer size;

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }
}
