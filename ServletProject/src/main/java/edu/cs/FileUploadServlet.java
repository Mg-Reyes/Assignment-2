package edu.cs;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
**/

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/FileUploadServlet")
@MultipartConfig(fileSizeThreshold=1024*1024*10, 	// 10 MB 
               maxFileSize=1024*1024*50,      	// 50 MB
               maxRequestSize=1024*1024*100)   	// 100 MB
public class FileUploadServlet extends HttpServlet {

  private static final long serialVersionUID = 205242440643911308L;
	
  /**
   * Directory where uploaded files will be saved, its relative to
   * the web application directory.
   */
  private static final String UPLOAD_DIR = "uploads";
  
  protected void doPost(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
	  try {
	  // gets absolute path of the web application
      String applicationPath = request.getServletContext().getRealPath("");

      // constructs path of the directory to save uploaded file
      String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;

       
      // creates the save directory if it does not exists
      File fileSaveDir = new File(uploadFilePath);
      if (!fileSaveDir.exists()) {
          fileSaveDir.mkdirs();
      }
      System.out.println("Upload File Directory="+fileSaveDir.getAbsolutePath());
      
      // data that will be retrieved from file and stored into files table in mySQL database
      String fileName = "";
      byte[] fileData = null;
      String mimeType = "";
      long fileSize = 0;
      long maxFileSize=1024*1024*50;
      
      //Get all the parts from request and write it to the file on server
      
	      for (Part part : request.getParts()) {
	    	  // retrieving the important data
	          fileName = getFileName(part);

	          fileData = part.getInputStream().readAllBytes();
	          mimeType = part.getContentType();
	          fileSize = part.getSize();
	        	  
	          fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
	          part.write(uploadFilePath + File.separator + fileName);
	      }
	     
	      if ( fileSize > maxFileSize )
	      {
	    	  response.getWriter().write("File rejected! File is too large.<br>");
	    	  response.getWriter().write("Max allowed: 50MB<br>");
	    	  return;
	      }
	      
      
    String message = "Result";
    try {
	    String content = new Scanner(new File(uploadFilePath + File.separator + fileName)).useDelimiter("\\Z").next();      
	    response.getWriter().write(message + "<BR>" + content);
    }
    catch(Exception e) 
    {
    	response.getWriter().write(message + " File Uploaded" );
    	
    }
    
    /****** Integrate remote DB connection with this servlet, uncomment and modify the code below *******
	   //ADD YOUR CODE HERE!
    ********/
    
    Connection con = null;
    PreparedStatement psInsertFile = null;
    String currIP = "localhost";
    String url = "jdbc:mysql://" + currIP + ":3306/testDB?" + "user=mz&password=Real-12"
    		+ "&useSSL=true"
            + "&requireSSL=true"
            + "&trustCertificateKeyStoreUrl=file:" + System.getProperty("user.dir") + "/truststore.jks"
            + "&trustCertificateKeyStorePassword=trust123"
            + "&clientCertificateKeyStoreUrl=file:" + System.getProperty("user.dir") + "/keystore.jks"
            + "&clientCertificateKeyStorePassword=trust123";
    
    String i1 = "INSERT INTO files (filename, file_data, file_type, file_size) VALUES (?, ?, ?, ?)";
    int queryResult = 0;
    
    try {
    	Class.forName("com.mysql.cj.jdbc.Driver");
		con = DriverManager.getConnection(url);
		
		
		psInsertFile = con.prepareStatement(i1);
		
		psInsertFile.setString(1, fileName);
	    psInsertFile.setBytes(2, fileData);
	    psInsertFile.setString(3, mimeType);
	    psInsertFile.setLong(4, fileSize);
	   
		queryResult = psInsertFile.executeUpdate();
		response.getWriter().write("DB Connection Success<br>");
		
		psInsertFile.close();
		con.close();
		
	} catch (SQLException | ClassNotFoundException e) {
		// TODO Auto-generated catch block
		response.getWriter().write("DB Connection Failed: " + e.getMessage());
		return;
	}
    
	  }catch( Exception e ) {
    	  response.getWriter().write("File rejected! File is too large. <br>");
    	  response.getWriter().write("Max allowed: 50MB<br>\n");
    	  response.getWriter().write("Error: " + e.getMessage());
    	  return;
      }
    
    //request.setAttribute("message", "File uploaded successfully!");
    //getServletContext().getRequestDispatcher("/response.jsp").forward(request, response);
 
    //Below is added for parsing EHR
    //DecodeCCDA parsed = new DecodeCCDA(uploadFilePath + File.separator + fileName);
    //writeToResponse(response, parsed.getjson());
  
  }

  /**
   * Utility method to get file name from HTTP header content-disposition
   */
  private String getFileName(Part part) {
      String contentDisp = part.getHeader("content-disposition");
      System.out.println("content-disposition header= "+contentDisp);
      String[] tokens = contentDisp.split(";");
      for (String token : tokens) {
          if (token.trim().startsWith("filename")) {
              return token.substring(token.indexOf("=") + 2, token.length()-1);
          }
      }
      return "";
  }
  
	private void writeToResponse(HttpServletResponse resp, String results) throws IOException {
		PrintWriter writer = new PrintWriter(resp.getOutputStream());
		resp.setContentType("text/plain");

		if (results.isEmpty()) {
			writer.write("No results found.");
		} else {
			writer.write(results);
		}
		writer.close();
	}	
}
