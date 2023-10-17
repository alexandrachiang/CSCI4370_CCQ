package com.IMDb.controller;

import com.IMDb.data.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class InsertController {
    String url = "jdbc:mysql://localhost:33306/Project_2";
    String user = "root";
    String password = "mysqlpass";
    
    @GetMapping("/insert")
    public ModelAndView leaderboard() {
        ModelAndView mv = new ModelAndView("insert");

        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            Statement stmt = connection.createStatement();
            String query = "SELECT * FROM Ratings ORDER BY rating_id DESC";
            String movie_query = "SELECT * FROM Movie";            
            ResultSet rs = stmt.executeQuery(query);
            List<Rating> ratings = new ArrayList<>();            
            while (rs.next()) {
                Rating rating = new Rating(rs.getInt("rating_id"), rs.getString("real_name"), rs.getString("username"), 
                        rs.getString("movie_name"), rs.getDouble("user_rating"), rs.getString("user_review"));
                ratings.add(rating);
                
            } // while            
            ResultSet movie_rs = stmt.executeQuery(movie_query);
            List<Movie> movies = new ArrayList<>();
            while (movie_rs.next()) {
                Movie movie = new Movie(movie_rs.getInt("rank"), movie_rs.getString("movie_name"), movie_rs.getString("release_year"), 
                		movie_rs.getFloat("imdb_rating"), movie_rs.getInt("duration"), movie_rs.getString("genre"));
                movies.add(movie);
            } // while
            
            mv.addObject("ratings", ratings);
            mv.addObject("movies", movies);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to the database!", e);
        }

        return mv;
    }
    
    @PostMapping("/submitinsert")
    public ModelAndView formsubmit(String real_name, String username, String movie_name, double user_rating, String user_review) {
        
        try {
            if (user_rating < 0 || user_rating > 10) {
                throw new IllegalArgumentException("User Rating must be between 0.0 and 10.0.");          
            } else if (real_name == null || username == null || movie_name == null || user_review == null) {
                throw new IllegalArgumentException("All fields are required.");
            }
            
            user_rating = (double) Math.round(user_rating * 10) / 10;

            Connection connection = DriverManager.getConnection(url, user, password);
            
            Statement stmt = connection.createStatement();
            
            String maxId = "SELECT MAX(rating_id) AS \"max\" FROM Ratings";
            ResultSet rs = stmt.executeQuery(maxId);

            rs.next();
            int rating_id = rs.getInt("max") + 1;
                        
            String ins = "INSERT INTO Ratings VALUES ('" + rating_id + "','" + real_name + "','" + username + "','" + movie_name + 
                    "','" + user_rating + "','" + user_review + "')";
            stmt.executeUpdate(ins);
   
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to the database!", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("All fields are required. User Rating must be between 0.0 and 10.0.", e);
        }
        
        return new ModelAndView("redirect:/insert");
    }
}
