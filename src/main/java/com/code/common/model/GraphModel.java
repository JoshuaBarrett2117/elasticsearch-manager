package com.code.common.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * @Description 基本的model
 * @Author liufei
 * @Date 2020/1/14 14:44
 */
public class GraphModel<MODEL extends GraphModel,
        IN_MODEL extends GraphModel, OUT_MODEL extends GraphModel> implements Serializable {
    protected String id;
    protected String name;
    protected String code;
    protected Collection<IN_MODEL> inModel;
    protected Collection<OUT_MODEL> outModel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Collection<IN_MODEL> getInModel() {
        return inModel;
    }

    public void setInModel(Collection<IN_MODEL> inModel) {
        this.inModel = inModel;
    }

    public Collection<OUT_MODEL> getOutModel() {
        return outModel;
    }

    public void setOutModel(Collection<OUT_MODEL> outModel) {
        this.outModel = outModel;
    }
}
