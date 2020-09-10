package eu.epitech.cashmanager2.cashmanager;

import eu.epitech.cashmanager2.cashmanager.model.Item;
import eu.epitech.cashmanager2.cashmanager.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Class holding data for an instantiated article
 */
@Configurable
public class Article {
    private ItemRepository itemRepository;
    private Item base;
    private int qty;

    public Article(String name, ItemRepository itemRepository) {
        this(name, 1, itemRepository);
    }

    public Article(String name, int qty, ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.base = itemRepository.findByName(name).orElseThrow();
        this.qty = qty;
    }

    private Article() {}

    public static Article mock(String name, double price, int qty) {
        var a = new Article();
        a.base = new Item();
        a.base.Name = name;
        a.base.Price = price;
        a.qty = qty;
        return a;
    }

    public String getName() {
        return base.Name;
    }

    public double getPrice() {
        return base.Price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void addQty(int qty) {
        this.qty += qty;
    }

    @Override
    public String toString() {
        return base.Name + ";" + base.Price + ";" + qty;
    }

    @Override
    public int hashCode() {
        return base.Name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Article)) {
            return false;
        }
        return base.Name.equals(((Article) obj).base.Name);
    }
}
