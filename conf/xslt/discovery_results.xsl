<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dyn="http://exslt.org/dynamic"
                extension-element-prefixes="dyn">
    <xsl:output method="html"/>
    <xsl:param name="lang" select="'de'"/>
    <xsl:param name="path" select="'./'"/>
    <xsl:param name="handle"/>
    <xsl:param name="basehandle"/>
    <xsl:variable name="translationpath"><xsl:value-of select="$path"/></xsl:variable>
    <xsl:variable name="language"><xsl:value-of select="$lang"/></xsl:variable>

    <xsl:template match="text()|@*"></xsl:template>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template name="translate">
        <xsl:param name="token" />
        <xsl:variable name="translations" select="document(concat($translationpath, $language, '.xml'))/translations" />
        <xsl:choose>
            <xsl:when test="$translations and $translations/phrase[@token = $token]">
                <xsl:value-of select="$translations/phrase[@token = $token]"/>
            </xsl:when>
            <xsl:otherwise>
                Phrase not found   <xsl:value-of select="$token"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="//div[@id='aspect.discovery.SimpleSearch.div.search-results']">
    <!-- xsl:template match="//list[@n='item-result-list']" -->
        <div id="paging"><xsl:value-of select="@itemsTotal"/> Items <xsl:value-of select="@pagesTotal"/> pagesTotal</div>

        <ul class="list-group">
        <xsl:for-each select="list/list[@n='item-result-list']/list">
            <li class="list-group-item">
                <xsl:variable name="id" select="substring-after(list[contains(@n,'dc.identifier.uri')]/item,concat($basehandle,'/'))"/>
                <div class="listitemheader"><a href="/item/{$handle}/{$id}"><xsl:value-of select="list[contains(@n,'dc.title')]/item"/></a> </div>
                <div class="listitemcontributor">
                    <xsl:for-each select="list[contains(@n,'dc.contributor')]/item">
                        <xsl:value-of select="."/>;
                    </xsl:for-each>
                </div>
                <div class="listitemdate"><xsl:value-of select="list[contains(@n,'dc.date.issued')]/item"/> </div>
         </li>
        </xsl:for-each>
        </ul>
    </xsl:template>




</xsl:stylesheet>