package com.worm.toolkit.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * 富文本 XSS 净化：按白名单过滤标签与属性，移除脚本协议属性与高危元素
 *
 * @author worm
 */
public class EscapeUtils {

    // 为什么：白名单沿用旧清单原顺序并剔除 applet/script/iframe 等 16 个高危标签 —— 干什么：限定可存活元素集合
    private static final String[] COMMON_TAGS = {
            "a", "abbr", "acronym", "address", "area", "article", "aside", "audio",
            "b", "bdi", "bdo", "big", "blockquote", "body", "br", "button",
            "canvas", "caption", "center", "cite", "code", "col", "colgroup", "datalist", "dd",
            "del", "details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "fieldset",
            "figcaption", "figure", "font", "footer", "form", "h1", "h2",
            "h3", "h4", "h5", "h6", "head", "header", "hr", "html", "i",
            "input", "ins", "kbd", "label", "legend", "li", "main", "map",
            "mark", "menu", "menuitem", "meter", "nav",
            "ol", "optgroup", "option", "output", "p", "picture", "pre", "progress",
            "q", "rp", "rt", "ruby", "s", "samp", "section", "select", "small",
            "source", "span", "strike", "strong", "sub", "summary", "sup", "table",
            "tbody", "td", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track",
            "tt", "u", "ul", "var", "video", "wbr"
    };

    private static PolicyFactory getPolicy() {
        return new HtmlPolicyBuilder()
                .allowElements(COMMON_TAGS)
                // 无属性元素仍保留（场景 1/2 的 <a>/<div> 存活依赖此项；若该 API 语义与预期不符，
                // 允许改用等效构造达成同样行为）
                .allowWithoutAttributes(COMMON_TAGS)
                .allowElements("img")
                .allowAttributes("src", "alt", "width", "height").onElements("img")
                // href 显式放行后由协议校验拦截 javascript: —— 场景 1 的「协议属性被移除」由此成立
                .allowAttributes("href").onElements("a")
                .allowUrlProtocols("http", "https", "data")
                .toFactory();
    }

    /**
     * 按白名单净化富文本，过滤 XSS 脚本向量
     *
     * @param richText 待净化的富文本
     * @return 净化后的内容
     */
    public static String sanitize(String richText) {
        Document doc = Jsoup.parse(richText);
        // 为什么：script 等高危元素须连内容整体移除，仅靠白名单摘标签会残留内联脚本文本 —— 干什么：预处理剥离高危元素子树
        doc.select("script,iframe,frame,frameset,applet,embed,object,noscript,noframes,style").remove();
        String cleanHtml = getPolicy().sanitize(doc.body().html());
        Document cleanDoc = Jsoup.parse(cleanHtml);
        return getPolicy().sanitize(cleanDoc.body().html());
    }
}
