package com.example.PRIEE;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class PrieeController {
    factory fac = new factory();

    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<Map<String, Object>> inventoryList = new ArrayList<>();
        ResultSet rs = fac.getFactory();

        try {
            while (rs.next()) {
                System.out.print(rs.getString("state")+" ");
                System.out.print(rs.getString("name")+" ");
                System.out.print(rs.getString("production")+" ");
                System.out.print(rs.getString("size")+" ");
                System.out.println(rs.getString("machine_count")+" ");
                Map<String, Object> item = new HashMap<>();
                item.put("state", rs.getString("state"));
                item.put("name", rs.getString("name"));
                item.put("production", rs.getInt("production"));
                item.put("size", rs.getInt("size"));
                item.put("machine_count", rs.getInt("machine_count"));
                inventoryList.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("inventoryList", inventoryList);
        return "inventory"; // Ensure inventory.html exists
    }
    @GetMapping("/view")
public String view(
    @RequestParam("state") String state, 
    @RequestParam(value = "failureRate", required = false) Double failureRate, 
    Model model) {

    factory fac = new factory();
    List<Map<String, Object>> inventory = new ArrayList<>();
    List<Map<String, Object>> lanes = new ArrayList<>();

    ResultSet rs = fac.getInventory(state);
    ResultSet rs2 = fac.getLanes(state);

    try {
        while (rs.next()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", rs.getString("name"));
            item.put("stock", rs.getInt("stock"));
            item.put("location", rs.getString("location"));
            item.put("category", rs.getString("category")); // Ensure this field exists
            item.put("supplier", rs.getString("supplier")); // Ensure this field exists
            item.put("price", rs.getDouble("price")); // Ensure this field exists
            inventory.add(item);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    try {
        while (rs2.next()) {
            Map<String, Object> lane = new HashMap<>();
            String laneValue = rs2.getString("lane");

            int laneNumber = Integer.parseInt(laneValue.replaceAll("[^0-9]", "")) * 10;
            int production = (laneNumber * 1000) / 100;

            lane.put("lane", laneValue);
            lane.put("production", production);
            lanes.add(lane);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }    

    model.addAttribute("inventory", inventory);
    model.addAttribute("lanes", lanes);
    
    if (failureRate != null) {
        model.addAttribute("failureRate", failureRate);
        System.out.println("Failure Rate received: " + failureRate);
    } else {
        model.addAttribute("failureRate", "N/A");
    }

    // Log the inventory and lanes for debugging
    System.out.println("Inventory: " + inventory);
    System.out.println("Lanes: " + lanes);

    return "view"; // Ensure you have a "view.html" file
}
@GetMapping("/laneDetails")
public String laneDetails(@RequestParam("lane") String lane, Model model) {
    factory fac = new factory();
    ResultSet rs3 = fac.getMachine();
    List<Map<String, Object>> machines = new ArrayList<>();

    try {
        while (rs3.next()) {
            Map<String, Object> machine = new HashMap<>();
            machine.put("name", rs3.getString("mac_id"));
            machine.put("efficiency", rs3.getInt("efficieny"));  // ✅ FIXED: Integer instead of String
            machine.put("status", rs3.getInt("prod_rate"));
            machines.add(machine);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    model.addAttribute("machines", machines);
    return "laneDetails"; // ✅ Make sure "laneDetails.html" exists
}
    @GetMapping("/order")
        public String order(){
            return "order";
        }// Ensure order.html exists
    @GetMapping("/checkout")
        public String checkout(){
            return "checkout";
        }
}

