package kr.co.klnet.aos.etransdriving.model;

import com.google.api.services.vision.v1.model.EntityAnnotation;

public class CloudTextListItem {
    private EntityAnnotation cloudEntity;
    private String description;

    public CloudTextListItem(EntityAnnotation entity){
        this.cloudEntity = entity;
        this.description = entity.getDescription();
    }

    public CloudTextListItem(String description){
        this.description = description;
    }

    public EntityAnnotation getCloudEntity()
    {
        return this.cloudEntity;
    }
    public String getDescription() {
        return this.description;
    }
}