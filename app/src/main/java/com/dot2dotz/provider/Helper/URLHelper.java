package com.dot2dotz.provider.Helper;

/**
 * Created by jayakumar on 26/12/16.
 */

public class URLHelper {
    //public static final String base = "http://159.89.37.53/";
    public static final String base = "https://dot2dotz.com/";
    //public static final String base = "http://6586b14b.ngrok.io/";
    public static final String HELP_URL = base+"";
    public static final String CALL_PHONE = "1";
    public static final String APP_URL = "https://play.google.com/store/apps/details?id=com.dot2dotz.provider&hl=en";
    //public static final String login = base + "api/provider/oauth/token";//Email Login
    public static final String login = base + "api/provider/loginpro";
    public static final String register = base + "api/provider/register";
    public static final String CHECK_MAIL_ALREADY_REGISTERED = base+"api/provider/verify";
    public static final String USER_PROFILE_API = base + "api/provider/profile";
    public static final String UPDATE_AVAILABILITY_API = base + "api/provider/profile/available";
    public static final String GET_HISTORY_API = base + "api/provider/requests/history";
    public static final String GET_HISTORY_DETAILS_API = base + "api/provider/requests/history/details";
    public static final String CHANGE_PASSWORD_API = base + "api/provider/profile/password";
    public static final String UPCOMING_TRIP_DETAILS = base + "api/provider/requests/upcoming/details";
    public static final String UPCOMING_TRIPS = base + "api/provider/requests/upcoming";
    public static final String CANCEL_REQUEST_API = base + "api/provider/cancel";
    public static final String TARGET_API = base + "api/provider/target";
    public static final String RESET_PASSWORD = base + "api/provider/reset/password";
    public static final String FORGET_PASSWORD = base + "api/provider/forgot/password";
    public static final String FACEBOOK_LOGIN = base + "api/provider/auth/facebook";
    public static final String GOOGLE_LOGIN = base + "api/provider/auth/google";
    public static final String LOGOUT = base + "api/provider/logout";
    public static final String SUMMARY = base + "api/provider/summary";
    public static final String HELP = base + "api/provider/help";
    public static final String GET_SERVICE_TYPE = base + "api/provider/service_type";
    public static final String GET_DOC = base + "api/provider/documents";
    public static final String GET_REGISTER_DOC = base + "api/provider/document/types";
    public static final String UPDATE_DOC = base + "api/provider/documentupload";
    public static final String GET_DOC_PAGE = base + "api/provider/documents";
    public static final String DOC_EXPIRE_UPLOAD = base + "api/provider/expireupload";
    public static final String GET_FEEDBACK = base + "api/provider/cancel/reasons";
}
