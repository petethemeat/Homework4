
import java.util.ArrayList;

public class Inventory {
	class Product {
		private String name;
		private int quantity;

		public Product(String name, int quantity) {
			this.setName(name);
			this.setQuantity(quantity);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		@Override
		public boolean equals(Object o) {

			if (o == this) {
				return true;
			}

			if (!(o instanceof Product)) {
				return false;
			}

			Product other = (Product) o;

			if (this.getName().equalsIgnoreCase(other.getName())) {
				return true;
			}
			return false;
		}
	}

	ArrayList<Product> productList;

	public Inventory() {
		this.productList = new ArrayList<Product>();
	}

	public void insert(String productName, int quantity) {
		Product newProduct = new Product(productName, quantity);
		productList.add(newProduct);
	}

	public synchronized boolean purchase(Product product, int quantity) {

		if(product.getQuantity() >= quantity){
			product.setQuantity(product.getQuantity()-quantity);
			return true;
		}
		
		return false;
		
	}

	public synchronized boolean cancel(String productName, int quantity) {

		int len = productList.size();
		Product p = getProduct(productName);
		
		for (int i = 0; i < len; i++) {
			if (p.equals(productList.get(i))) {
				productList.get(i).setQuantity(p.getQuantity() + quantity);
				return true;
			}
		}
		return false;

	}

	public Product getProduct(String productName) {
		int len = productList.size();

		for (int i = 0; i < len; i++) {
			if (productList.get(i).getName().contentEquals(productName)) {
				return productList.get(i);
			}
		}

		return null;
	}

	public synchronized ArrayList<Product> list() {
		return this.productList;
	}

}
