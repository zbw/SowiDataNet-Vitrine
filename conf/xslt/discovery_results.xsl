<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dyn="http://exslt.org/dynamic"
                extension-element-prefixes="dyn">
    <xsl:output method="html"/>
    <xsl:param name="lang" select="'de'"/>
    <xsl:param name="path" select="'./'"/>
    <xsl:param name="handle"/>
    <xsl:param name="institut.id"/>
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
        <div id="paging">

            <ul class="pager">
            <xsl:choose>
                <xsl:when test="@currentPage > 1">
                    <li class="previous">
                    <a class="btn btn-default">
                        <xsl:variable name="backlink" select="@pageURLMask"/>
                        <xsl:variable name="lastpage" select="@currentPage -1"/>
                        <xsl:variable name="backlink" select="replace($backlink, '\{pageNum\}', string($lastpage))"/>
                    <xsl:attribute name="href"><xsl:value-of select="replace($backlink, 'discover', '')"/>> </xsl:attribute>
                        <xsl:call-template name="translate"><xsl:with-param name="token">pager.back</xsl:with-param></xsl:call-template>
                    </a>
                    </li>
                </xsl:when>
            </xsl:choose>
                <div class="pagerinfo">
                    <xsl:value-of select="@itemsTotal"/>&#160; <xsl:call-template name="translate"><xsl:with-param name="token">pager.items</xsl:with-param></xsl:call-template>&#160; |&#160; <xsl:call-template name="translate"><xsl:with-param name="token">pager.page</xsl:with-param></xsl:call-template> &#160;<xsl:value-of select="@currentPage"/> &#160;<xsl:call-template name="translate"><xsl:with-param name="token">pager.of</xsl:with-param></xsl:call-template>&#160; <xsl:value-of select="@pagesTotal"/>&#160;<xsl:call-template name="translate"><xsl:with-param name="token">pager.pages</xsl:with-param></xsl:call-template>
                </div>
            <xsl:choose>
                <xsl:when test="@currentPage != @pagesTotal">
                    <li class="next">
                    <a class="btn btn-default">
                        <xsl:variable name="backlink" select="@pageURLMask"/>
                        <xsl:variable name="lastpage" select="@currentPage +1"/>
                        <xsl:variable name="backlink" select="replace($backlink, '\{pageNum\}', string($lastpage))"/>
                        <xsl:attribute name="href"><xsl:value-of select="replace($backlink, 'discover', '')"/>> </xsl:attribute>
                        <xsl:call-template name="translate">
                            <xsl:with-param name="token">pager.forward</xsl:with-param>
                        </xsl:call-template>
                    </a>
                    </li>
                </xsl:when>
            </xsl:choose>
            </ul>

        </div>

        <ul class="list-group">
        <xsl:for-each select="list/list[@n='item-result-list']/list">
            <li class="list-group-item">
                <xsl:variable name="id" select="substring-after(list[contains(@n,'dc.identifier.uri')]/item,concat($basehandle,'/'))"/>
                <h4 class="listitemheader"><a href="/{$institut.id}/item/{$handle}/{$id}"><xsl:value-of select="list[contains(@n,'dc.title')]/item"/></a> </h4>
                <div class="listitem">
                <div class="listitemlabel"><xsl:call-template name="translate"><xsl:with-param name="token">label.researcher</xsl:with-param></xsl:call-template></div>
                <div class="listitemvalue">
                    <xsl:for-each select="list[ends-with(@n,'dbk.primaryresearcher')]/item">
                        <div><xsl:value-of select="."/></div>
                    </xsl:for-each>
                </div>
                </div>
                <div class="listitem">
                <div class="listitemlabel"><xsl:call-template name="translate"><xsl:with-param name="token">label.pubyear</xsl:with-param></xsl:call-template></div>
                    <div class="listitemvalue"><xsl:value-of select="list[contains(@n,'publicationyear')]/item"/> </div>
                </div>
         </li>
        </xsl:for-each>
        </ul>
    </xsl:template>




</xsl:stylesheet>