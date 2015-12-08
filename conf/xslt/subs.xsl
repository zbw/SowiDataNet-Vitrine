<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dyn="http://exslt.org/dynamic"
                extension-element-prefixes="dyn">
    <xsl:output method="html"/>
    <xsl:param name="lang" select="'de'"/>
    <xsl:param name="path" select="'./'"/>
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

    <xsl:template match="/document/options/list[@id='aspect.discovery.Navigation.list.discovery']">



        <xsl:for-each select="list">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">

                        <xsl:variable name="titletag" select="normalize-space(head/.)"/>
                        <xsl:call-template name="translate">
                            <xsl:with-param name="token"><xsl:value-of select="$titletag" /></xsl:with-param>
                        </xsl:call-template>
                    </h3>
                </div>
            <div class="panel-body">
        <xsl:for-each select="item">
            <div>
                <a>
                <xsl:attribute name="href"><xsl:value-of select="xref/@target"/></xsl:attribute>
                <xsl:value-of select="xref/."/>
                </a>
            </div>
        </xsl:for-each>
            </div>
            </div>
        </xsl:for-each>
    </xsl:template>




</xsl:stylesheet>