/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2014 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *************************************************************************/

package tds.student.sbacossmerge.data;

import java.util.UUID;

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Web.EncryptionHelper;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mskhan
 * 
 */
// / <summary>
// / GeoComponent provides all information needed to access a distributed
// component.
// / </summary>
// TODO: Shajib
// [DataContract(Name = "geo")]
public class GeoComponent
{
  // / <summary>
  // / Unique service key for this component.
  // / </summary>
  @JsonProperty ("key")
  private UUID    _key;

  // / <summary>
  // / The type of geo database.
  // / </summary>
  @JsonProperty ("type")
  private GeoType _type;

  // / <summary>
  // / The database name.
  // / </summary>
  private String  _dBName;

  // / <summary>
  // / The database IP address.
  // / </summary>
  private String  _dBIP;

  // / <summary>
  // / The database username for logging in.
  // / </summary>
  private String  _dBUser;

  // / <summary>
  // / The database password for logging in.
  // / </summary>
  private String  _dBPassword;

  // / <summary>
  // / Application active version
  // / </summary>
  private int     _activeVersion;

  // / <summary>
  // / URL provides an application with a means of communicating with a
  // component across network boundaries
  // / For some cases, the browser is to transfer to the URL (i.e. Login and
  // Satellite for Proctor and Student)
  // / In other cases, the URL represents a web service to send/receive data
  // (e.g. Hub and shard for Student)
  // / In these other cases, if URL is null and Linked is 'false', then assume
  // the hub and shard database servers are directly accessible
  // / </summary>
  @JsonProperty ("url")
  private String  _url;

  // / <summary>
  // / Are the databases directly linked? If so, then proctor and student ONLY
  // talk directly to LOGIN and SATELLITE
  // / If they are NOT linked, then proctor and student must manage ALL
  // GeoComponent communications
  // / </summary>
  // [DataMember(Name = "linked")]
  // public bool Linked { get; private set; }

  
  private String _connectionStringEncrypted;
  private String _connectionString;

  public GeoComponent ()
  {
  }

  public GeoComponent (GeoType type, UUID key, String db, String ip, String url)
  {
    setType (type);
    setKey (key);
    setDBName (db);
    setDBIP (ip);
    setUrl (url);
  }

  public GeoComponent (GeoType type, UUID key, String cs)
  {

  }

  // / <summary>
  // / Return a connection String builder.
  // / </summary>
  // / <returns></returns>

  // TODO Shajib: implement later
  /*
   * private SqlConnectionStringBuilder createConnectionString () { if (_dBName
   * == null || _dBIP == null || _dBUser == null || _dBPassword == null) {
   * return null; }
   * 
   * SqlConnectionStringBuilder builder = new SqlConnectionStringBuilder ();
   * builder.InitialCatalog = _dBName; builder.DataSource = _dBIP;
   * builder.UserID = _dBUser; builder.Password = _dBPassword; return builder; }
   */

  // / <summary>
  // / Return a connection String.
  // / </summary>
  // / <example>
  // / User ID=dbtds;password=ABCDEF;Initial Catalog=TDSGEO_Dev_Satellite1;Data
  // Source=38.118.82.148
  // / </example>

  public String getConnectionString ()
  {
    // TODO Shajib
    String csBuilder = null;// createConnectionString ();
    return csBuilder != null ? csBuilder.toString () : null;
  }

  public void setConnectionString(String value)
  {
    //Not required.
  }
  
  @JsonProperty ("cs")
  public String getConnectionStringEncrypted ()
  {
    return "N/A";
  }

  public void setConnectionStringEncrypted (String value)
  {
    // Not implemented.
  }

  // / <summary>
  // / Set the appID and password
  // / </summary>
  public void setLogin (String user, String password)
  {
    setDBUser (user);
    setDBPassword (password);
  }

  public void setLogin (GeoComponent geoComponent)
  {
    setLogin (geoComponent.getDBUser (), geoComponent.getDBPassword ());
  }

  // / <summary>
  // / Create and parse geo information from a sql reader.
  // / </summary>
  private static GeoComponent Parse (GeoType type, boolean usePrivate, DbResultRecord /* IComlumnReader */reader)
  {
    // TODO Shajib
    /*
     * Guid key = reader.GetGuid ("serviceKey"); String dbName =
     * reader.GetString ("dbname"); String dbIP = reader.GetString ("dbIP");
     * 
     * // check if we should use the private IP if (usePrivate &&
     * reader.HasValue ("privateIP")) { dbIP = reader.GetString ("privateIP"); }
     * 
     * String url = null; int version = 0;
     * 
     * // get url (only for login and sat) if (!reader.IsDBNull ("URL")) { url =
     * reader.getString ("URL"); }
     * 
     * // check for version # (some sp's don't have this column) if
     * (reader.HasValue ("ActiveVersion")) { version = reader.GetInt32
     * ("ActiveVersion"); }
     * 
     * GeoComponent geo = new GeoComponent (type, key, dbName, dbIP, url);
     * geo.ActiveVersion = version;
     * 
     * return geo;
     */
    return null;
  }

  // / <summary>
  // / Parse the public (frontend) geo information.
  // / </summary>
  // / <remarks>
  // / Use the DB info for the servers in the same component.
  // / For example: Sat Web --> Sat DB
  // / </remarks>
  public static GeoComponent parse (GeoType type, DbResultRecord reader)
  {
    // TODO : Shajib
    /* return parse (type, false, reader); */
    return null;
  }

  // / <summary>
  // / Parse the private (backend) geo information.
  // / </summary>
  // / <remarks>
  // / Use the DB info for the servers cross component.
  // / For example: Sat Web --> Hub DB
  // / </remarks>
  public static GeoComponent ParsePrivate (GeoType type, DbResultRecord reader)
  {
    return Parse (type, true, reader);
  }

  public static GeoComponent ReadAndParse (GeoType type, DbResultRecord reader)
  {
    // check if the right columns exist and read first row
    // TODO: Shajib reader.read()
    if (reader.hasColumn ("serviceKey") && reader.hasColumn ("serviceType") /*
                                                                             * &&
                                                                             * reader
                                                                             * .
                                                                             * read
                                                                             * (
                                                                             * )
                                                                             */)
    {
      return parse (type, reader);
    }

    return null;
  }

  public UUID getKey () {
    return _key;
  }

  private void setKey (UUID value) {
    this._key = value;
  }

  public GeoType getType () {
    return _type;
  }

  private void setType (GeoType value) {
    this._type = value;
  }

  public String getDBName () {
    return _dBName;
  }

  private void setDBName (String value) {
    this._dBName = value;
  }

  public String getDBIP () {
    return _dBIP;
  }

  private void setDBIP (String value) {
    this._dBIP = value;
  }

  public String getDBUser () {
    return _dBUser;
  }

  private void setDBUser (String value) {
    this._dBUser = value;
  }

  public String getDBPassword () {
    return _dBPassword;
  }

  private void setDBPassword (String value) {
    this._dBPassword = value;
  }

  public int getActiveVersion () {
    return _activeVersion;
  }

  public void setActiveVersion (int value) {
    this._activeVersion = value;
  }

  public String getUrl () {
    return _url;
  }

  public void setUrl (String value) {
    this._url = value;
  }
}
