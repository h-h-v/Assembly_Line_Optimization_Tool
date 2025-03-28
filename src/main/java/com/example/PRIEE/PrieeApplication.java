package com.example.PRIEE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.zone.*;

@SpringBootApplication
public class PrieeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrieeApplication.class, args);
		String x = null;
		System.out.println(" ");
		Scanner sc = new Scanner(System.in);
		while(x != "exit"){
			System.out.print("hhv$: ");
			x = sc.nextLine();
			if (x.equals("inventory")){
				System.out.println("Loading....");
				factory f = new factory();
				f.getInventory("chennai");
			}
			else if(x.equals("factory")){
				System.out.println("Loading....");
				factory f = new factory();
				f.getFactory();
			}
			else if(x.equals("order")){
				System.out.println("Loading....");
				String location;
				int quantity;
				String date;
				System.out.print("Enter the location: ");
				location = sc.nextLine();
				
				System.out.print("Enter the Quantity: ");
				quantity = sc.nextInt();
				sc.nextLine();  // Consume the leftover newline
				
				System.out.print("Enter the date: ");
				date = sc.nextLine();
				
				order o = new order(quantity, location, date);
				
			}
			else{
				System.out.println("Invalid command");
			}
		}
	}

}

class conn {
    String url = "jdbc:postgresql://stingily-arresting-fieldmouse.data-1.use1.tembo.io:5432/postgres";
    String user = "postgres";
    String password = "ZF79AnmP8WTITQmg";
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
class layout{
	
}

class factory{
	conn c = new conn();
	Connection conn = c.connect();
	ResultSet rs = null;
	public ResultSet getInventory(String x) {
		try {
			rs = conn.createStatement().executeQuery("SELECT * FROM inventory where location = '"+x+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	/*public ResultSet displayInventory() {
		try {
			rs = conn.createStatement().executeQuery("SELECT * FROM inventory");
			while(rs.next()){
				System.out.print(rs.getString("sno")+" ");
				System.out.print(rs.getString("name")+" ");
				System.out.print(rs.getString("stock")+" ");
				System.out.println(rs.getString("location"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}*/
	public ResultSet getFactory() {
		try {
			rs = conn.createStatement().executeQuery("SELECT * FROM factory");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	public ResultSet getMachine() {
		try {
			rs = conn.createStatement().executeQuery("SELECT * FROM machines");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	public ResultSet getLanes(String x) {
		try {
			rs = conn.createStatement().executeQuery("SELECT lane.fac_id,lane.lane FROM lane,factory where factory.state = '"+x+"' AND lane.fac_id = factory.name");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
}
class order{
	int order_id = 1;
	int quantity;
	String delivery;
	String date;
public order(int quantity, String delivery, String date){
	this.quantity = quantity;
	this.delivery = delivery;
	this.date = date;
	conn c = new conn();
	Connection conn = c.connect();
	int stock = 0;	
	int production = 0;
	System.out.println("Checking Inventory....");
	try {
		ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM inventory,factory where inventory.location = '"+delivery+"'"+"AND factory.state = '"+delivery+"'"+ "AND factory.state = inventory.location");
		while(rs.next()){
			if(rs.getInt("stock") >= quantity){
					System.out.println("Order placed successfully");
					System.out.println("Order ID: "+order_id);
					System.out.println("Quantity: "+quantity);
					System.out.println("stock: "+stock);
					System.out.println("Production: "+production);
					System.out.println("State: "+rs.getString("location"));
					int rowsAffected = conn.createStatement().executeUpdate(
					"UPDATE inventory SET stock = stock - " + quantity + " WHERE sno = " + rs.getInt("sno")
				);
				if (rowsAffected > 0) {
					System.out.println("Stock updated successfully!");
				}
					break;	
			}
			else if (rs.getInt("stock") < quantity) {
				stock = rs.getInt("stock");
				production = quantity - stock;
				
				// Get current date
				LocalDate currentDate = LocalDate.now();
				
				// Parse the input date correctly
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
				LocalDate dt = LocalDate.parse(date.trim(), formatter);
				
				// Calculate the days difference
				long prodt = ChronoUnit.DAYS.between(currentDate, dt);
			
				// Fix production calculation to avoid rounding issues
				double requiredDays = (double) production / (8 * rs.getInt("production"));
				
				int rowsAffected = conn.createStatement().executeUpdate(
					"UPDATE inventory SET stock = 0  WHERE sno = " + rs.getInt("sno")
				);
				if (rowsAffected > 0) {
					System.out.println("Stock updated successfully!");
				}

				System.out.println("requiredDays: " + requiredDays);
				System.out.println("prodt: " + prodt);
				if (requiredDays <= prodt) {
					System.out.print(prodt);
				}
				if(requiredDays > prodt){
					ResultSet ls = conn.createStatement().executeQuery("SELECT * FROM nearst where location = '"+delivery+"'");
					if(ls.next()){
					for(int i=1;i<=3;i++){
						try{
							String loc = ls.getString("n"+i);
							ResultSet rs1 = conn.createStatement().executeQuery("SELECT * FROM inventory where inventory.location = '"+loc+"'");
							if(rs1.next()){
							if(rs1.getInt("stock") >= production){
								System.out.println("Order placed successfully");
								System.out.println("Order ID: "+order_id);
								System.out.println("Quantity: "+quantity);
								System.out.println("stock: "+stock);
								System.out.println("Production: "+production);
								System.out.println("Factories: "+rs.getString("location")+","+rs1.getString("location"));
								rowsAffected = conn.createStatement().executeUpdate(
								"UPDATE inventory SET stock = stock - " + quantity + " WHERE sno = " + rs1.getInt("sno")
							);
							if (rowsAffected > 0) {
								System.out.println("Stock updated successfully!");
							}
								} else {
									System.out.println("Order cannot be placed");
									break;
								}
						}
							} catch(SQLException e){
								e.printStackTrace();
							}
						}
					}
					} 
					
					else if(quantity > 0){
						getOptimizedLanes(rs.getString("name"), production, conn);
					}
				}
			} 
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public List<Integer> getOptimizedLanes(String fac_id, int requiredProduction, Connection conn) {
		List<Integer> lanes = new ArrayList<>();
		try {
			// Fetch lanes sorted in descending order (higher lane number = more efficient)
			String query = "SELECT lane FROM lane WHERE fac_id = ? ORDER BY lane DESC";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, fac_id);
			ResultSet rs = ps.executeQuery();
			
			int remainingProduction = requiredProduction;
	
			while (rs.next() && remainingProduction > 0) {
				int lane = rs.getInt("lane");
				System.out.println("Lane: " + lane);
				lanes.add(lane);
				remainingProduction -= getLaneProductionRate(lane);  // Function to get production rate
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lanes;
	}
	
	// Example: Function to return production rate per lane
	public int getLaneProductionRate(int lane) {
		return 10 * lane;  // Example: Production rate increases with lane number
	}
	
}