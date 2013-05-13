package edu.uw.danco.account;

import edu.uw.ext.framework.account.Address;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/13/13
 * Time: 6:16 PM
 */
public class AddressImpl implements Address {
    /** serialVersionUID */
    private static final long serialVersionUID = 7549265121331107862L;

    /** Factor used in calculating hashCode. */
    private static final int HASH_FACTOR = 37;

    /** Street number. */
    private String streetAddress;

    /** City name. */
    private String city;

    /** State or province. */
    private String state;

    /** Postal or zip code. */
    private String zipCode;

    /** Hash code value. */
    private Integer hashCode;

    /**
     * Return the street address
     * @return - the street address
     */
    @Override
    public String getStreetAddress() {
        return streetAddress;
    }


    /**
     * Update the street address
     * @param streetAddress - the address to store
     */
    @Override
    public void setStreetAddress(final String streetAddress) {
        this.streetAddress = streetAddress == null ? "" : streetAddress;
    }


    /**
     * Get the city value for the address
     * @return - the city
     */
    @Override
    public String getCity() {
        return city;
    }


    /**
     * Update the city value
     * @param city - the value to store
     */
    @Override
    public void setCity(final String city) {
        this.city = city == null ? "" : city;
    }


    /**
     * The state where the address is located
     * @return - the state value
     */
    @Override
    public String getState() {
        return state;
    }


    /**
     * Update the state
     * @param state - the state value to store
     */
    @Override
    public void setState(final String state) {
        this.state = state == null ? "" : state;
    }


    /**
     * Get the zip code
     * @return - the zip code
     */
    @Override
    public String getZipCode() {
        return zipCode;
    }


    /**
     * Update the zip
     * @param zip - the value to store as the zip code
     */
    @Override
    public void setZipCode(final String zip) {
        this.zipCode = zip == null ? "" : zip;
    }


    /**
     * Concatenates the street, city, state and zip properties into the standard
     * one line postal format.
     *
     * @return the formated address string
     */
    public String toString() {
        return String.format("streetAddress=%s%n" +
                             "city=%s%n" +
                             "state=%s%n" +
                             "zipCode=%s%n", streetAddress, city, state, zipCode);
    }
}
