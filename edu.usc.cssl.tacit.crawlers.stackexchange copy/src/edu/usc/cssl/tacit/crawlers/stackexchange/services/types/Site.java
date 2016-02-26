package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import java.util.Date;
import java.util.List;

public class Site {

    protected List<String> aliases;
    protected Styling styling;
    protected List<RelatedSite> relatedSites;
    protected List<String> markdownExtensions;
    protected Date launchDate;
    protected String siteState;
    protected String highResolutionIconUrl;
    protected String faviconUrl;
    protected String iconUrl;
    protected String audience;
    protected String siteUrl;
    protected String apiSiteParameter;
    protected String logoUrl;
    protected String name;
    protected String siteType;


    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public Styling getStyling() {
        return styling;
    }

    public void setStyling(Styling styling) {
        this.styling = styling;
    }

    public List<RelatedSite> getRelatedSites() {
        return relatedSites;
    }

    public void setRelatedSites(List<RelatedSite> relatedSites) {
        this.relatedSites = relatedSites;
    }

    public List<String> getMarkdownExtensions() {
        return markdownExtensions;
    }

    public void setMarkdownExtensions(List<String> markdownExtensions) {
        this.markdownExtensions = markdownExtensions;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public String getSiteState() {
        return siteState;
    }

    public void setSiteState(String siteState) {
        this.siteState = siteState;
    }

    public String getHighResolutionIconUrl() {
        return highResolutionIconUrl;
    }

    public void setHighResolutionIconUrl(String highResolutionIconUrl) {
        this.highResolutionIconUrl = highResolutionIconUrl;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getApiSiteParameter() {
        return apiSiteParameter;
    }

    public void setApiSiteParameter(String apiSiteParameter) {
        this.apiSiteParameter = apiSiteParameter;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public class Styling {

        protected String tagBackgroundColor;
        protected String tagForegroundColor;
        protected String linkColor;

        public String getTagBackgroundColor() {
            return tagBackgroundColor;
        }

        public void setTagBackgroundColor(String tagBackgroundColor) {
            this.tagBackgroundColor = tagBackgroundColor;
        }

        public String getTagForegroundColor() {
            return tagForegroundColor;
        }

        public void setTagForegroundColor(String tagForegroundColor) {
            this.tagForegroundColor = tagForegroundColor;
        }

        public String getLinkColor() {
            return linkColor;
        }

        public void setLinkColor(String linkColor) {
            this.linkColor = linkColor;
        }

    }

    public class RelatedSite {

        protected String relation;
        protected String siteUrl;
        protected String name;

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }

        public String getSiteUrl() {
            return siteUrl;
        }

        public void setSiteUrl(String siteUrl) {
            this.siteUrl = siteUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
