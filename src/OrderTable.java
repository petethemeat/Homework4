import java.util.ArrayList;

public class OrderTable {

	class Order {
		private String productName, userName;
		private int quantity;
		private int orderId;

		public Order(String userName, String productName, int quantity, int orderId) {
			setUserName(userName);
			setProductName(productName);
			setQuantity(quantity);
			setOrderId(orderId);
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public int getOrderId() {
			return orderId;
		}

		public void setOrderId(int orderId) {
			this.orderId = orderId;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (!(o instanceof Order)) {
				return false;
			}

			Order other = (Order) o;

			return other.orderId == getOrderId();
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}
	}

	ArrayList<Order> orders;
	Inventory inv;

	public OrderTable() {
		orders = new ArrayList<Order>();
	}

	public void listOrders() {
		System.out.println("Order List");
	}

	public synchronized int order(String userName, String productName, int quantity) {
		int orderId = orders.size() + 1;
		Order newOrder = new Order(userName, productName, quantity, orderId);
		orders.add(newOrder);
		newOrder.setOrderId(orderId);
		return orderId;
	}

	public Order[] search(String userName) {
		ArrayList<Order> orderList = new ArrayList<Order>();

		for (Order order : orders) {
			if (order.getUserName().contentEquals(userName)) {
				orderList.add(order);
			}
		}

		Order[] retArray = new Order[orderList.size()];
		retArray = orderList.toArray(retArray);

		return retArray;
	}

	public synchronized boolean cancel(int orderId, Inventory inv) {
		int len = orders.size();

		for (int i = 0; i < len; i++) {
			if (orders.get(i).getOrderId() == orderId) {
				inv.cancel(orders.get(i).productName, orders.get(i).quantity);
				orders.remove(i);
				return true;
			}
		}
		return false;
	}

}
