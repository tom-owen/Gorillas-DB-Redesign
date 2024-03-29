package ROMdb.Models;

import ROMdb.Driver.Main;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anthony Orio on 3/27/2017.
 */
public class EstimationBaseModel {


    // For calculating staff day.
    private static final double STAFF_MONTH_DIVISOR = 20.92;
    public static HashMap<String, Integer> baselineByName = new HashMap<>();
    public static HashMap<Integer, String> baselineByID = new HashMap<>();

    /**
     * This method will evaluate each text field and ensure that
     * no illegal values are inserted into the fields. If fields
     * are suppose to contain numbers, then they will be compared
     * to a specific number format pattern. If fields are left blank,
     * then the user will be told to enter a value. Finally, the
     * weight fields will be evaluated to ensure they add up to 100.
     * @param baseline baseline to check input for.
     * @param staffDay staffDay will be automatically generated.
     * @param staffMonth The input should be a number.
     * @param integration Integration should be between 0 and 100 inclusively.
     * @param testing Testing should be between 0 and 100 inclusively.
     * @param code Code should be between 0 and 100 inclusively.
     * @param defaultSlocs Checks for valid input of defaultSlocs.
     * @param design Design should be between 0 and 100 inclusively.
     * @param upgrade The upgrade value to check input for..
     * @param maint Maintenance to check input for.
     * @param ddr DDR to check input for.
     * @param document Document value to check input for.
     * @param date Date value to check input for.
     * @param cprs CPRS value to check input for.
     * @throws SQLException If the statement could not be successfully completed.
     */
    public static void errorChecking(String baseline, String staffDay, String staffMonth, String integration, String testing, String code,
                                     String defaultSlocs, String design, String upgrade, String maint,
                                     String ddr, String document, String date, String cprs) throws SQLException{

        // Stores the error message if any.
        String error = "";

        // Stores the text from the next evaluated field.
        String parsed = "";

        // The pattern correlates to any decimal number up to 25
        // decimal digits.
        String pattern = "\\d+(\\.\\d{1,25})?";

        // Flag if an error is found during any checks.
        boolean errorExists = false;


        // SLOCS/Staff-Month check
        parsed = staffMonth.trim();
        if (!parsed.matches(pattern))
        {
            error += "SLOCs/Staff Month\n";
            errorExists = true;
        }

        // Integration Weight check
        parsed = integration;
        if (!parsed.matches(pattern))
        {
            error += "Integration Weight\n";
            errorExists = true;
        }

        // Unit Testing Weight check
        parsed = testing;
        if (!parsed.matches(pattern))
        {
            error += "Unit Testing Weight\n";
            errorExists = true;
        }

        // Code Weight check
        parsed = code;
        if (!parsed.matches(pattern))
        {
            error += "Code Weight\n";
            errorExists = true;
        }

        // Default SLOCS check
        parsed = defaultSlocs;
        if (!parsed.matches(pattern))
        {
            error += "Default SLOCS\n";
            errorExists = true;
        }

        // Design Weight check
        parsed = design;
        if (!parsed.matches(pattern))
        {
            error += "Design Weight\n";
            errorExists = true;
        }

        // Budget Upgrade check
        parsed = upgrade;
        if (!parsed.matches(pattern))
        {
            error += "Budget Upgrade\n";
            errorExists = true;
        }

        // Budget Maintenance check
        parsed = maint;
        if (!parsed.matches(pattern))
        {
            error += "Budget Maintenance\n";
            errorExists = true;
        }

        // DDR/CWT/SLOCS check
        parsed = ddr;
        if (!parsed.matches(pattern))
        {
            error += "DDR CWR SLOCS\n";
            errorExists = true;
        }

        // CPDD Document check
        if (document == null || document.trim().equals(""))
        {
            error += "CPDD Document\n";
            errorExists = true;
        }

        // CPDD Date check
        // can be blank

        // CPRS# check
        if (cprs == null || cprs.trim().equals(""))
        {
            error += "CPRS#\n";
            errorExists = true;
        }


        if (errorExists)    // If flag set during evaluations.
        {
            // Assemble the error message.
            error = "You've entered the wrong input for: \n" + error;

            // Display box containing the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR, error, ButtonType.OK);
            alert.showAndWait();

            return;     // Do not continue
        }


        // We need to check to make sure the weights add to 100.
        if (   (Double.parseDouble(design)        +
                Double.parseDouble(code)          +
                Double.parseDouble(integration)   +
                Double.parseDouble(testing)   ) != 100)
        {

            // Assemble the error message.
            String weightError = "Your weights do not add up to 100";

            // Display box containing the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR, weightError, ButtonType.OK);
            alert.showAndWait();

            return;     // Do not continue
        }

        // All evaluations have passed.
        // Call method to write the valid data.
        writeTextfieldsToDB( baseline, staffDay, staffMonth,  integration,  testing,  code,
                 defaultSlocs,  design,  upgrade,  maint,
                 ddr,  document,  date,  cprs);
    }

    /**
     * This method will calculate the SLOCS/Staff-Day field
     * based on the following formula:
     *         (SLOCS/Staff-Month / 20.92) = SLOCS/Staff-Day
     * @param staffMonth the number entered in to the staff month text field
     * @return The string representation of the staff day value calculated.
     */

    public static String calculateStaffDay(String staffMonth)
    {
        // We will store the answer here.
        double staffDay;

        // The formatted result #.##
        String result = "";

        // If the field is currently empty then result is 0.0
        if (staffMonth.equals(""))
        {
            result = "0.0";
        }

        // Otherwise generate the value.
        else
        {
            // Declare the format
            DecimalFormat df = new DecimalFormat("#.##");

            // Find the value.
            staffDay = Double.parseDouble(staffMonth) / STAFF_MONTH_DIVISOR ;

            // Format the value.
            result = df.format(staffDay);
        }

        // Set the field text with the formatted value.
        //field_staffDay.setText(result);

        return result;
    }

    /**
     * This method will parse the information currently out of the text
     * fields and right them into the database column specified by the
     * names within the insertQuery string.
     * @param baseline the current baseline
     * @param staffDay the input for staff day
     * @param staffMonth the input for staff month
     * @param integration the input for integration weight
     * @param testing the input for testing weight
     * @param code the input for code weight
     * @param defaultSlocs the input for default slocs
     * @param design the input for design weight
     * @param upgrade the input for budget upgrade
     * @param maint the input for budget maintenance
     * @param ddr the input for DDR/CWT slocs
     * @param document the cpdd document
     * @param date the cpdd date
     * @param cprs the cprs number
     * @throws SQLException when the input is invalid
     */
    private static void writeTextfieldsToDB(String baseline, String staffDay, String staffMonth, String integration, String testing, String code,
                                    String defaultSlocs, String design, String upgrade, String maint,
                                    String ddr, String document, String date, String cprs) throws SQLException
    {
        // The currently selected baseline from the drop down.
        //String baseline = baseline.getSelectionModel().getSelectedItem();

        // The query to insert the data from the fields.
        String insertQuery = "UPDATE Baseline SET [cprs]=?, [slocs_per_day]=?, [slocs_per_month]=?, " +
                "[slocs_default]=?, [slocs_ddr_cwt]=?, [cpdd_document]=?, [cpdd_date]=?, [budget_upgrade]=?, " +
                "[budget_maintenance]=?, [design_weight]=?, [code_weight]=?, [integration_weight]=?, " +
                "[unit_test_weight]=? WHERE [baseline_desc]=?";

        // Create a new statement.
        PreparedStatement st = Main.newconn.prepareStatement(insertQuery);

        /** Parse all of the information and stage for writing. */
        st.setString(1, cprs);
        st.setString(2, staffDay);
        st.setString(3, staffMonth);
        st.setString(4, defaultSlocs);
        st.setString(5, ddr);
        st.setString(6, document);
        st.setString(7, date);
        st.setString(8, upgrade);
        st.setString(9, maint);
        st.setString(10, design);
        st.setString(11, code);
        st.setString(12, integration);
        st.setString(13, testing);
        st.setString(14, baseline);

        // Perform the update inside of the table of the database.
        st.executeUpdate();
    }

    /**
     * This method will take the information from the database table
     * "basicrom" and display it inside the correct text field when
     * the user chooses the baseline from the drop down menu. This information
     * can then be updated if desired.
     * @param baseline the selected baseline to fill in the values for
     * @return a list containing all the values for an estimation give a certain baseline
     * @throws SQLException when the input for baseline is incorrect or there is a problem reading from the database
     */
    public static ArrayList<String> fillTextFieldsFromDB(String baseline) throws SQLException
    {
        //updateCurrentBaseline();
        ArrayList<String> valuesFromDB = new ArrayList<>();
            // Create query
            String query = "SELECT * FROM Baseline WHERE baseline_desc=?";

            // Create the statement to send.
            PreparedStatement st = Main.newconn.prepareStatement(query);

            st.setString(1, baseline);

            // Return the result set from this query.
            ResultSet rs = st.executeQuery();

            while (rs.next()) // Retrieve data from ResultSet
            {
                /** Write all of the information into the correct text field. */

                valuesFromDB.add(rs.getString("cprs"));
                valuesFromDB.add(rs.getString("slocs_per_day"));
                valuesFromDB.add(rs.getString("slocs_per_month"));
                valuesFromDB.add(rs.getString("slocs_default"));
                valuesFromDB.add(rs.getString("slocs_ddr_cwt"));
                valuesFromDB.add(rs.getString("cpdd_document"));
                valuesFromDB.add(rs.getString("cpdd_date"));
                valuesFromDB.add(rs.getString("budget_upgrade"));
                valuesFromDB.add(rs.getString("budget_maintenance"));
                valuesFromDB.add(rs.getString("design_weight"));
                valuesFromDB.add(rs.getString("code_weight"));
                valuesFromDB.add(rs.getString("integration_weight"));
                valuesFromDB.add(rs.getString("unit_test_weight"));
            }
        return valuesFromDB;
    }

    public static void fillBaselineIDMap() throws SQLException
    {
        String query = "SELECT [baseline_id], [baseline_desc] FROM Baseline";
        PreparedStatement st = Main.newconn.prepareStatement(query);
        ResultSet rs = st.executeQuery();

        while (rs.next())
        {
            String desc = rs.getString("baseline_desc");
            int id = rs.getInt("baseline_id");

            baselineByName.put(desc, id);
            baselineByID.put(id, desc);
        }
    }
}
