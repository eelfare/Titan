package com.yunji.titanrtx.manager.service.report.support;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * MarkdownUtils
 *
 * @author leihz
 * @since 2020-05-13 11:07 上午
 */
public class MarkdownUtils {

    public static String renderToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

}
