package com.victor.spider.core.selector;

import org.apache.commons.collections.CollectionUtils;
import org.jsoup.nodes.Element;
import us.codecraft.xsoup.Xsoup;
import us.codecraft.xsoup.xevaluator.XPathEvaluator;

import java.util.List;

/**
 * XPath selector based on Xsoup.<br>
 */
public class XpathSelector extends BaseElementSelector {

    private XPathEvaluator xPathEvaluator;

    public XpathSelector(String xpathStr) {
        this.xPathEvaluator = Xsoup.compile(xpathStr);
    }

    @Override
    public String select(Element element) {
        return xPathEvaluator.evaluate(element).get();
    }

    @Override
    public List<String> selectList(Element element) {
        return xPathEvaluator.evaluate(element).list();
    }

    @Override
    public Element selectElement(Element element) {
        List<Element> elements = selectElements(element);
        if (CollectionUtils.isNotEmpty(elements)){
            return elements.get(0);
        }
        return null;
    }

    @Override
    public List<Element> selectElements(Element element) {
        return xPathEvaluator.evaluate(element).getElements();
    }

    @Override
    public boolean hasAttribute() {
        return xPathEvaluator.hasAttribute();
    }
}
