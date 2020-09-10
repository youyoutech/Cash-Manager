package eu.epitech.cashmanager2.cashmanager;

import java.util.LinkedList;
import java.util.List;

/**
 * Class holding the data for a client's cart
 */
public class Cart {
    private List<Article> articles = new LinkedList<>();

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder("cart");
        for (Article a : articles)
            tmp.append(" ").append(a.toString());
        return tmp.toString();
    }

    public void addArticle(Article a) {
        if (!articles.contains(a))
            articles.add(a);
        else
            articles.get(articles.indexOf(a)).addQty(a.getQty());
    }

    public boolean removeArticle(Article a) {
        if (!articles.contains(a))
            return false;
        articles.remove(a);
        return true;
    }

    public boolean reduceArticle(Article a) {
        if (!articles.contains(a))
            return false;
        var ar = articles.get(articles.indexOf(a));
        ar.addQty(-a.getQty());
        if (ar.getQty() <= 0)
            articles.remove(ar);
        return true;
    }

    public Article getArticle(Article a) {
        return !articles.contains(a) ? Article.mock(a.getName(), 0, 0) : articles.get(articles.indexOf(a));
    }

    public void clear() {
        articles.clear();
    }
}
