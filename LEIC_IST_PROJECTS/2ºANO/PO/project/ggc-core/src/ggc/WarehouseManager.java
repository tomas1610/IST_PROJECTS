package ggc;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import ggc.exceptions.*;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;

import ggc.Warehouse;

//FIXME import classes (cannot import from pt.tecnico or ggc.app)

/** Façade for access. */
public class WarehouseManager {

  /** Name of file storing current store. */
  private String _filename = "";

  private boolean _save = false;

  private Warehouse _warehouse = new Warehouse();

  public String showLessPurchases(){
    return _warehouse.showLessPurchases();
  }

  public void changeId(String actualKey, String newKey){
    _warehouse.changeId(actualKey,newKey);
  }

  public String showExpensiveProduct(){
    return _warehouse.showExpensiveProduct();
  }

  public int getDate(){
    return _warehouse.getDate_aux();
  }

  public Double getAvailable(){
    return _warehouse.getAvailable_aux();
  }

  public Double getSum(){
    return _warehouse.getSum_aux();
  }

  public boolean getSAVED(){
    return _save;
  }
  public void registPartner(String key,String name,String adress) throws DuplicateUIPartnerException{
    _warehouse.registPartAux(key,name,adress);
  }

  public void registBatchBuyS(String productId, String partnerId, Double price, int amount){
    _warehouse.registBatchS(productId,partnerId,price,amount);
  }

  public void registBatchBuyM(String productId, String partnerId, Double price, int amount, Double alpha, String recipe){
    _warehouse.registBatchM(productId, partnerId,price,amount,alpha,recipe);
  }

  public String recipeToString(LinkedHashMap<String,Integer> recipe){
    String text = "";
    for (String s : recipe.keySet()){
      int amount = recipe.get(s);
      text = text + s + ":" + amount + "#";
    }
    return text;
  }

  public String showTransaction(int key) throws UnknownTransactionException{
    if ((_warehouse.hasTransaction(key) == false) && (_warehouse.nonePayed(key) == false))
      throw new UnknownTransactionException(key);
    return _warehouse.getTransaction(key).toString();
  }


  public String showPartner(String key) throws ShowPartnerException{
    if ((_warehouse.hasPartner(key)) == false )
      throw new ShowPartnerException(key);
    return _warehouse.getPartner(key).toString();
  }

  public ArrayList<String> showNotificationsPartner(String partnerKey){
    Partner p = _warehouse.getPartner(partnerKey);
    return p.getNotifications();
  }

  public void clearNotifications(String partnerKey){
    Partner p = _warehouse.getPartner(partnerKey);
    p.clearNotifications();
  }

  public ArrayList<String> showAllPartners(){
    ArrayList<String> list = new ArrayList<String>();
    for (Partner p : _warehouse.getPartnersMap().values()){
      list.add(p.toString());
    }
    return list;
  }

  public ArrayList<String> showAllProducts(){
    ArrayList<String> list = new ArrayList<String>();
    for (Product p : _warehouse.getProductsMap().values()){
      list.add(p.toString());
    }
    return list;
  }

  public ArrayList<String> showAllAvailableBatches(){
    ArrayList<Batch> list = _warehouse.showBatches();
    ArrayList<String> batchString = new ArrayList<String>();
    for (Batch b : list){
      batchString.add(b.toString());
    }
    return batchString;
  }

  public ArrayList<String> showBatchesByProduct(String productId) throws UnknownProductException{
    if (_warehouse.hasProduct(productId) == false)
      throw new UnknownProductException(productId);
    ArrayList<Batch> list = _warehouse.showBatches();
    ArrayList<String> batchString = new ArrayList<String>();
    for (Batch b : list){
      if (b.getProductId().equals(productId)) {
        batchString.add(b.toString());
      }
    }
    return batchString;
  }

  public ArrayList<String> showBatchesByPartner(String partnerId) throws UnknownPartnerException{
    if (_warehouse.hasPartner(partnerId) == false)
      throw new UnknownPartnerException(partnerId);
    ArrayList<Batch> list = _warehouse.showBatches();
    ArrayList<String> batchString = new ArrayList<String>();
    for (Batch b : list){
      if (b.getPartnerId().equals(partnerId)) {
        batchString.add(b.toString());
      }
    }
    return batchString;
  }

  public ArrayList<String> showBatchesUnderPrice(Double price){
    ArrayList<Batch> list = _warehouse.showBatches();
    ArrayList<String> batchString = new ArrayList<String>();
    for (Batch b : list){
      if ((b.getPrice() < price))
        batchString.add(b.toString());
    }
    return batchString;
  }

  public ArrayList<String> showPaymentsByPartner(String partnerId) throws UnknownPartnerException{
    if (_warehouse.hasPartner(partnerId) == false)
      throw new UnknownPartnerException(partnerId);
    ArrayList<String> list = new ArrayList<String>();
    for (SalePaid v : _warehouse.getSalesMap().values()){
      list.add(v.toString());
    }
    return list;
  }

  public ArrayList<String> toggleNotifications(String partnerId, String productId) throws UnknownPartnerException,UnknownProductException{
    if ((_warehouse.hasProduct(productId)) == false)
      throw new UnknownProductException(productId);
    if ((_warehouse.hasPartner(partnerId)) == false )
      throw new UnknownPartnerException(partnerId);
    TreeMap<String,Product> products = _warehouse.getProductsMap();
    TreeMap<String,Partner> partners = _warehouse.getPartnersMap();
    Product p = products.get(productId);
    ArrayList<String> notifieds = p.getPartnersNotified();
    if (notifieds.contains(partnerId)){
      p.disablePartnerNoti(partnerId);
    }
    else if (partners.containsKey(partnerId)){
      p.enablePartnerNoti(partnerId);
    }
    return p.getPartnersNotified();
  }

  public boolean productVerification(String productId){
    if (_warehouse.hasProduct(productId))
      return true;
    return false;
  }

  public void registBreakDown(String partnerId, String productId, int amount) throws UnavailableProductUIException, UnknownPartnerException, UnknownProductException{
    if (_warehouse.hasPartner(partnerId) == false)
      throw new UnknownPartnerException(partnerId);
    if (_warehouse.hasProduct(productId) == false)
      throw new UnknownProductException(productId);
    if (_warehouse.isDerived(productId) == true)
      _warehouse.registeBreakDown(partnerId,productId,amount);
  }

  public void registeAcquisition(String partnerId,String productId,Double price, int amount) throws UnknownPartnerException{
    if (_warehouse.hasPartner(partnerId) == false)
      throw new UnknownPartnerException(partnerId);
    _warehouse.registePurchaseS(partnerId,productId,price,amount);
  }

  public void registeSale(String partnerId, int date, String productId, int amount) throws UnavailableProductUIException,UnknownProductException, UnknownPartnerException{
    if (_warehouse.hasPartner(partnerId) == false)
      throw new UnknownPartnerException(partnerId);
    if (_warehouse.hasProduct(productId) == false)
      throw new UnknownProductException(productId);
    _warehouse.registeSale(partnerId,productId,amount,date);
  }

  public void pay(int key) throws UnknownTransactionException{
    if (key < 0 || key > _warehouse.getTransactionUID()){
      throw new UnknownTransactionException(key);
    }
    if (_warehouse.nonePayed(key)){
      _warehouse.payment(key);
    }
  }

  public ArrayList<String> showPartnerAcquisition(String key) throws UnknownPartnerException{
    if (_warehouse.hasPartner(key) == false)
      throw new UnknownPartnerException(key);
    ArrayList<String> list = new ArrayList<String>();
    for (Purchase c : _warehouse.getPurchasesMap().values()){
      if (c.getPartnerId().equals(key))
        list.add(c.toString());
    }
    return list;
  }

  public ArrayList<String> showPartnerSales(String key) throws UnknownPartnerException{
    if (_warehouse.hasPartner(key) == false)
      throw new UnknownPartnerException(key);
    ArrayList<String> list = new ArrayList<String>();
    for (Transaction t : _warehouse.getAllSalesMap().values()){
      if (t.getPartnerId().equals(key))
        list.add(t.toString());
    }

    return list;
  }

  public void advanceDate(int n) throws NegativeDateException{
    if ( n <= 0)
      throw new NegativeDateException(n);
    _warehouse.addDays(n);
  }


  public String getFile(){
    return _filename;
  }

  /**
   * @@throws IOException
   * @@throws FileNotFoundException
   * @@throws MissingFileAssociationException
   */
  public void save() throws IOException, FileNotFoundException, MissingFileAssociationException {
    if (_filename == "")
      throw new MissingFileAssociationException();

      ObjectOutputStream in = new ObjectOutputStream(new FileOutputStream(_filename));
      in.writeObject(_warehouse);
      in.close();
    }
  


  /**
   * @@param filename
   * @@throws MissingFileAssociationException
   * @@throws IOException
   * @@throws FileNotFoundException
   */
  public void saveAs(String filename) throws MissingFileAssociationException, FileNotFoundException, IOException {
    _filename = filename;
    save();
  }

  /**
   * @@param filename
   * @@throws UnavailableFileException
   */
  public void load(String filename) throws UnavailableFileException {
    try  {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
      _warehouse = (Warehouse) in.readObject();
      _filename = filename;
    }
    catch (IOException | ClassNotFoundException e){
      throw new UnavailableFileException(filename);
    } 
  }

  /**
   * @param textfile
   * @throws ImportFileException
   */
  public void importFile(String textfile) throws ImportFileException{
    try {
	    _warehouse.importFile(textfile);
    } catch (IOException | BadEntryException | DuplicateUIPartnerException e) {
	    throw new ImportFileException(textfile);
    }
  }
}


