package ggc;
import java.io.Serializable;
import java.io.IOException;
import java.io.FileNotFoundException;
import ggc.exceptions.BadEntryException;
import ggc.exceptions.ImportFileException;
import ggc.exceptions.MissingFileAssociationException;
import ggc.exceptions.UnavailableFileException;
import ggc.exceptions.DuplicateUIPartnerException;
import ggc.exceptions.UnavailableProductUIException;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;

// FIXME import classes (cannot import from pt.tecnico or ggc.app)

/**
 * A Warehouse has batches (Packs of Products) for its Partners that can buy or sell batches 
 */
public class Warehouse implements Serializable {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 202108111602L;
 
  private int _date = 0;
  private Double _sales = 0.00;
  private Double _purchases = 0.00;
  private int _batchUID = 0;
  private int _transactionUID = 0;

  TreeMap<String,Partner> _partners = new TreeMap<String,Partner>(String.CASE_INSENSITIVE_ORDER);
  TreeMap<Integer,Batch> _batches = new TreeMap<Integer,Batch>();
  TreeMap<String,Product> _products = new TreeMap<String,Product>(String.CASE_INSENSITIVE_ORDER);
  TreeMap<Integer,Transaction> _transactions = new TreeMap<Integer,Transaction>();
  TreeMap<Integer,Purchase> _purchasesMap = new TreeMap<Integer,Purchase>();
  TreeMap<Integer,SalePaid> _salesMap = new TreeMap<Integer,SalePaid>();
  TreeMap<Integer,Transaction> _allSalesMap = new TreeMap<Integer,Transaction>();
  HashMap<String,ArrayList<String>> _notifications = new HashMap<String,ArrayList<String>>();
  HashMap<Integer,SaleNotPaid> _salesNotPaid = new HashMap<Integer,SaleNotPaid>();

  /** 
  * Regists one Product into the TreeMap
  * @param productId  Id of the product that we want to regist
  * @param price  price of the Product
  * @param stock  quantity of the Product
  * @param partnerId  Id of the Partner associated with the Product
  * @return Nothing
  */

  public String showLessPurchases(){
    String string = "";
    for (Partner p1 : _partners.values()){
      Double purchases = p1.getPurchases();
      for (Partner p2 : _partners.values()){
        if (p2.getPurchases() < purchases){
          string = p2.toString();
          purchases = p2.getPurchases();
        }
      }
      break;
    }
    return string;
  }

  public void changeId(String actualKey, String newKey){
    Partner p = _partners.get(actualKey);
    _partners.remove(actualKey,p);
    p.setId(newKey);
    _partners.put(newKey,p);
  }

  public String showExpensiveProduct(){
    Double price = 0.00;
    String string = "";
    for (Product p : _products.values()){
      if (p.getPrice() >= price){
        price = p.getPrice();
        string = p.toString();
      }
    }
    return string;
  }

  /**
  * Returns an ArrayList with all the Batches to be shown
  * @return lista ArrayList whit all the Batches
   */

  public ArrayList<Batch> showBatches(){
    ArrayList<Batch> list = new ArrayList<Batch>();
    for (Batch b : getBatchesMap().values()){
      list.add(b);
    }
    list.sort(new Compare());
    return list;
  }

  /**
  * regists a PurhcaseTransaction in the system
  * @param partnerKey String PartnerId
  * @param productKey String ProductId
  * @param price Double price of the sale 
  * @param amount Integer amount that we want to Buy
  * @return nothing
    */

  public void registePurchaseS(String partnerKey,String productKey,Double price,int amount){
    registBatchS(productKey,partnerKey,price,amount);
    Purchase c = new Purchase(_transactionUID,partnerKey,productKey,amount,price * amount,getDate_aux());
    _purchases += price * amount;
    Partner partner = getPartner(partnerKey);
    partner.setPurchases(partner.getPurchases() + price*amount);
    _transactions.put(_transactionUID,c);
    _purchasesMap.put(_transactionUID,c);
    _transactionUID += 1;
  }

  /**
  * check if there are availableBatches from the Product
  * @param productId String representing the ProductId
  * @return boolean true or false if there are any batches 
  */

  public boolean batchCheck(String productId){
    for (Batch b : _batches.values()){
      if (b.getProductId().equals(productId))
        return true;
    }
    return false;
  }

  /** Receives a Product and returns the id of the cheapest Batch of that product
  * @param p receives a Product to check what is the cheapest Batch
  * @return int corresponding to Cheapest Batch 
  */

  public int getCheaperBatch(Product p){
    String productId = p.getId();
    ArrayList<Batch> batches = new ArrayList<Batch>();
    Batch cheaper;
    int return_value = 0;
    Double price = p.getPrice();
    for (Batch b : _batches.values()){
      if (b.getProductId().equals(p.getId())){
        batches.add(b);
      }
    }
    for (Batch b : batches){
      if (b.getPrice() <= price){
        cheaper = b;
        price = cheaper.getPrice();
        return_value = cheaper.getId();
      }
    }
    return return_value;
  }

  /** Check if the batch stock is 0 , if it is remove the Batch from the memory 
  * @param batch  Batch to check the stock
  * @return nothing */

  public void checkIfRemove(Batch batch){
    int batchUID = batch.getId();
    if (batch.getStock() == 0)
      _batches.remove(batchUID);

  }

  /**
  * Checks if we have enought components , to make the agregation we want
  * @param productId String representing the product we want to create
  * @param amountLeft int representing the amount we need
  * @return nothing if it is possile , if it is not , throws Exception */

  public void checkIfPossibleAgregation(String productId, int amountLeft) throws UnavailableProductUIException{
    Product p = getProduct(productId);
    MixedProduct product = (MixedProduct) p;
    String recipe = product.getRecipe();
    String recipeItem[] = recipe.split("#");
    for (String composition : recipeItem){
      String component[] = composition.split(":");
      int componentAmount = Integer.parseInt(component[1]);
      Product productAux = getProduct(component[0]);
      if (productAux.getStock() < amountLeft * componentAmount)
        throw new UnavailableProductUIException(component[0],amountLeft * componentAmount, productAux.getStock());
    }
  }

  /** Return the price of the agregation and registry the Agregation Transaction in the memory and update stocks
  * @param productId String representing the product we want to agregate
  * @param amountLeft int representing the amount left 
  * @param price Double that represents de price of the agregation
  * @param alpha Double to calculate price
  * @return Double returns the price of the agregation
   */

  public Double registAgregation(String productId,int amountLeft, Double price, Double alpha){
    if (amountLeft == 0){    
      return price;
    }
    Product p = getProduct(productId);
    Double result = price;
    price = 0.00;
    MixedProduct product = (MixedProduct) p;
    String recipe = product.getRecipe();
    String recipeItem[] = recipe.split("#");
    for (String composition : recipeItem){
      String component[] = composition.split(":");
      Product productAux = getProduct(component[0]);
      Batch b = getBatch(getCheaperBatch(productAux));
      int componentAmount = Integer.parseInt(component[1]);
      productAux.setStock(productAux.getStock() - componentAmount);
      while (componentAmount > 0){
      b = getBatch(getCheaperBatch(productAux));
      int stock = b.getStock();
      if (stock >= componentAmount){
        b.setStock(stock - componentAmount);
        price += b.getPrice() * componentAmount;
        componentAmount = 0;
      }
      else {
        componentAmount -= stock;
        price += b.getPrice() * stock;
        b.setStock(0);
      }
      checkIfRemove(b);
      }
    }
    if ( (price*(1 + alpha)) > p.getPrice()){
      p.setPrice(price*(1+alpha));
    }
    result += price * (1+ alpha);
    return registAgregation(productId,amountLeft - 1, result,alpha);
    
  }

  /** Registy the BreakDown int the memory , and update Stocks
  * @param partnerId String that represents the Partner
  * @param productId String that represents the Product
  * @param amount int that represents the amount we want to desagregate
  * @return nothing */

  public void registeBreakDown(String partnerId, String productId, int amount) throws UnavailableProductUIException{
    Product product = getProduct(productId);
    int s = product.getStock();
    if (s < amount)
      throw new UnavailableProductUIException(productId,amount,s);
    Partner partner = getPartner(partnerId);
    MixedProduct tproduct = (MixedProduct) product;
    String recipe = tproduct.getRecipe();
    String recipeItem[] = recipe.split("#");
    Double priceS = 0.00;
    Double price ;
    String recipeToString = "";
    for (String str : recipeItem){
      String component[] = str.split(":");
      String componentId = component[0];
      int componentAmount = Integer.parseInt(component[1]);
      if (batchCheck(componentId) == true){
        Batch b = getBatch(getCheaperBatch(getProduct(componentId)));
        price = b.getPrice() * componentAmount * amount;
        recipeToString = recipeToString + componentId + ":" + componentAmount * amount + ":" + Math.round(price) + "#";
      }
      else{
        Product componentP = getProduct(componentId);
        price = componentP.getPrice() * componentAmount * amount;
        recipeToString = recipeToString + componentId + ":" + componentAmount * amount + ":" + Math.round(price) + "#";
      }
      if (isDerived(componentId) == false)
        registBatchS(partnerId,componentId,price,amount*componentAmount);
      else {
        registBatchM(partnerId,componentId,price,amount*componentAmount,0.00,"");
      }
      priceS += price;
      Product componentP= getProduct(componentId);
      componentP.setStock(componentP.getStock() + componentAmount * amount);        
    }
    int left = amount;
    Double priceM = 0.00 ;
    Breakdown d;
    while (left > 0){
      int batchUID = getCheaperBatch(tproduct);
      Batch bCheapest = getBatch(batchUID);
      int stock = bCheapest.getStock();
      if (stock >= left){
        bCheapest.setStock(stock -left);
        priceM += bCheapest.getPrice() * left;
        left = 0;
        checkIfRemove(bCheapest);
      }
      else {
        left = left - stock;
        priceM += stock * bCheapest.getPrice();
        bCheapest.setStock(0);
        checkIfRemove(bCheapest);
      }
    }
    Double baseValue = priceM - priceS;
    recipeToString = recipeToString.substring(0,recipeToString.length() - 1);
    if (baseValue < 0){
      d = new Breakdown(_transactionUID,partnerId,productId,amount,baseValue,0.00,recipeToString,getDate_aux());
    }
    else {
      partner.setPoints(baseValue* 10 + partner.getPoints());
      d = new Breakdown(_transactionUID,partnerId,productId,amount,baseValue,baseValue,recipeToString,getDate_aux());
    }
    tproduct.setStock(tproduct.getStock() - amount);
    _transactions.put(_transactionUID,d);
    _allSalesMap.put(_transactionUID,d);
    _transactionUID += 1;
  }

  /** Regists Sales int the memory and update stocks
  * @param partnerKey String partnerKey that represents the partner
  * @param productKey String productKey that represents the product
  * @param amount int that represents the amount of the sale
  * @param date int that represents the day of the sale
  * @return nothing */

  public void registeSale(String partnerKey, String productKey, int amount, int date) throws UnavailableProductUIException{
    Double price = 0.00;
    Product p = getProduct(productKey);
    Partner partner = getPartner(partnerKey);
    int stock = p.getStock();
    if (stock < amount && isDerived(productKey) == false)
      throw new UnavailableProductUIException(productKey,amount,stock);
    if (stock < amount && isDerived(productKey) == true){
      checkIfPossibleAgregation(productKey,amount - stock);
    }
    int left = amount;
    MixedProduct m;
    while (left > 0){
      if (batchCheck(productKey) == false){
        m = (MixedProduct) p;
        price += registAgregation(productKey,left,0.00,m.getAlpha());
        break;
      }
      int batchUID = getCheaperBatch(p);
      Batch b = getBatch(batchUID);
      stock = b.getStock();
      if (stock >= left){
        b.setStock(stock -left);
        price += b.getPrice() * left;
        left = 0;
        checkIfRemove(b);
      }
      else {
        left = left - stock;
        price += stock * b.getPrice();
        b.setStock(0);
        checkIfRemove(b);
      }
    }
    if (amount > stock)
      p.setStock(0);
    else 
      p.setStock(stock-amount);
    partner.setSalesBase(partner.getSalesBase() + price);
    SaleNotPaid v = new SaleNotPaid(_transactionUID,partnerKey,productKey,amount,price,price,date);
    _salesNotPaid.put(_transactionUID,v);
    _allSalesMap.put(_transactionUID,v);
    _transactionUID += 1;
  }

  /** pays the transaction sale 
  * @param key int that represents the key of the transaction we want to pay 
  * @return nothing */

  public void payment(int key){
    SaleNotPaid v = _salesNotPaid.get(key);
    if ( _salesNotPaid.containsKey(key) == false)
      return;
    Product product = getProduct(v.getProductId());
    Partner partner = getPartner(v.getPartnerId());
    Double points = partner.getPoints();
    PartnerState status = partner.getStatus();
    Double price = partner.calculatePrice(v.getBaseValue(),v.getLimitDate(), getDate_aux(), product.getN());
    partner.setSalesReal(partner.getSalesReal() + price);
    SalePaid vp = new SalePaid(key,v.getPartnerId(),v.getProductId(),v.getAmount(),v.getBaseValue(),price,v.getLimitDate(),getDate_aux());
    _sales += vp.getAmount() * vp.getRealValue();
    _transactions.put(key,vp);
    _salesMap.put(key,vp);
    _allSalesMap.remove(key);
    _allSalesMap.put(key,vp);
    _salesNotPaid.remove(key);
  }
  
  /** calculate the balance of the nonePayed Transactions
  * @ return Double that represents the Balance of the SaleNot paids
   */

  public Double nonePayedBalance(){
    Double total = 0.00;
    for (SaleNotPaid vp : _salesNotPaid.values()){
      Product product = getProduct(vp.getProductId());
      Partner partner = getPartner(vp.getPartnerId());
      Double price = partner.calculatePrice(vp.getBaseValue(),getDate_aux(), vp.getLimitDate(), product.getN());
      total = total + price;
    }
    return total;
  }

  /** Initialize an Array with all the partners that will be notified
  * @return an ArrayList that corresponds to the Partner being notified by one product */

  public ArrayList<String> productNotifications(){
    ArrayList<String> notifications = new ArrayList<String>();
    for (Partner p : _partners.values()){
      notifications.add(p.getId());
    }
    return notifications;
  }

  /** Receives the type of the notification and throws the notification to the partners
  * @param productId String that represents the product 
  * @param type String that represents the Type of the notification we will throw
  * @param price Double that represents the price of the Batch
  * @return nothing  */

  public void throwNotification(String productId, String type, Double price){
    Product p = getProduct(productId);
    ArrayList<String> partners = p.getPartnersNotified();
    for (String s : partners){
      Partner partner = getPartner(s);
      Notification n = new Notification(productId,price,type);
      partner.addNotification(n.toString());
    } 
  }

  /** 
  * Regists one Simple Product into the TreeMap
  * @param productId  Id of the product that we want to regist
  * @param price  price of the Product
  * @param stock  quantity of the Product
  * @param partnerId  Id of the Partner associated with the Product
  * @return Nothing
  */

  public void registProductS(String productId, Double price, int stock, String partnerId){
    if (_products.containsKey(productId)){
      Product p = getProduct(productId);        
      int s = p.getStock();
      int batchUID = getCheaperBatch(p);
      Batch b = getBatch(batchUID);
      if (s == 0)
        throwNotification(productId,"NEW",price);
      p.setStock(s + stock);
      if (price >= p.getPrice())
        p.setPrice(price);
      if (price < b.getPrice()){
        throwNotification(productId,"BARGAIN",price);
        p.setCheaperBatch(_batchUID);
      }
    }
    else {
      ArrayList<String> notifications = new ArrayList<String>();
      notifications = productNotifications();
      _notifications.put(productId,notifications);
      Product p = new Product(productId,price,stock,partnerId,notifications,_transactionUID);
      _products.put(productId,p);
    }
  }

  /** 
  * Regists one Mixed Product into the TreeMap
  * @param productId  Id of the product that we want to regist
  * @param price  price of the Product
  * @param stock  quantity of the Product
  * @param partnerId  Id of the Partner associated with the Product
  * @param alpha Double 
  * @param recipe String 
  * @return Nothing
  */

  public void registProductM(String productId, Double price, int stock, String partnerId, Double alpha, String recipe){
    if (_products.containsKey(productId)){
      Product p = getProduct(productId);
      int s = p.getStock();
      int batchUID = p.getCheaperBatch();
      Batch b = getBatch(batchUID);
      if (s == 0)
        throwNotification(productId,"NEW",price);
      p.setStock(s + stock);
      if (price > p.getPrice())
        p.setPrice(price);
      if (price < b.getPrice()){
        throwNotification(productId,"BARGAIN",price);
        p.setCheaperBatch(_batchUID);
      }
    }
    else {
      ArrayList<String> notifications = new ArrayList<String>();
      notifications = productNotifications();
      _notifications.put(productId,notifications);
      MixedProduct p = new MixedProduct(productId,price,stock,partnerId,alpha,recipe,notifications,_transactionUID);
      _products.put(productId,p);
    }
  }

  /**
  * Regist a Simple Batch into the TreeMap
  * @param productId  Id of the Product associated with the batch
  * @param partnerId  Id of the Partner associated with the batch
  * @param price price of the batch
  * @param stock stock of products available on the batch
  * @return Nothing
   */

  public void registBatchS(String productId, String partnerId, Double price, int stock){
    registProductS(productId,price,stock,partnerId);
    Batch b = new Batch(_batchUID,partnerId,productId,price,stock);
    _batches.put(_batchUID,b);
    _batchUID += 1;
  }

  /**
  * Regist a Mixed Batch into the TreeMap
  * @param productId  Id of the Product associated with the batch
  * @param partnerId  Id of the Partner associated with the batch
  * @param price price of the batch
  * @param stock stock of products available on the batch
  * @param alpha Double that represents the alpha
  * @param recipe String recipe
  * @return Nothing
   */

  public void registBatchM(String productId, String partnerId, Double price, int stock, Double alpha, String recipe){
    registProductM(productId,price,stock,partnerId,alpha,recipe);
    Batch b = new Batch(_batchUID,partnerId,productId,price,stock);
    _batches.put(_batchUID,b);
    _batchUID += 1;
  }

  /**
  * Regist a Partner into the Treemap
  * @param key  ID of the Partner
  * @param name Name of Partner
  * @param adress Adress of Partner
  * @throws DuplicateUIPartnerException 
  * @return Nothing
   */

  public void registPartAux(String key,String name,String adress) throws DuplicateUIPartnerException{
    if (hasPartner(key))
      throw new DuplicateUIPartnerException(key);
    Partner p = new Partner(key,name,adress,0.00);
    _partners.put(key,p);
    for (Product product : getProductsMap().values()){
      product.enablePartnerNoti(key);
    }
  }

  /** Checks if it is Derived
  * @param productId String that represents the product we want to check 
  * @return Boolean that says if it is Derived*/

  public boolean isDerived(String productId){
  Product p = getProduct(productId);
  if (p.getN() == 5)
    return false;
  return true;
  }

  /**
  * Gets the TreeMap of Partners
  * @return TreeMap with String and Partner
   */

  public TreeMap<String, Partner> getPartnersMap(){
    return _partners;
  }

  /**
  * Gets the TreeMap of Purchases
  * @return TreeMap with Integer and Purchase
   */

  public TreeMap<Integer, Purchase> getPurchasesMap(){
    return _purchasesMap;
  }

  /**
  * Gets the TreeMap of SalesPaids
  * @return TreeMap with Integer and SalePaid
   */

  public TreeMap<Integer, SalePaid> getSalesMap(){
    return _salesMap;
  }

  /**
  * Gets the HashMap of SalesNot Paid
  * @return HashMap with Integer and SaleNotPaid
   */

  public HashMap<Integer, SaleNotPaid> getSalesNotPaidMap(){
    return _salesNotPaid;
  }

  /**
  * Gets the TreeMap of Products
  * @return TreeMap with String and Product
   */

  public TreeMap<String, Product> getProductsMap(){
    return _products;
  }

  /**
  * Gets the TreeMap of Batches
  * @return TreeMap with Integer(Serial Version UID of Batch) and Batch
   */
  public TreeMap<Integer, Batch> getBatchesMap(){
    return _batches;
  }

  /**
  * Gets the TreeMap of AllSales
  * @return TreeMap with Integer and Transaction */

  public TreeMap<Integer,Transaction> getAllSalesMap(){
    return _allSalesMap;
  }

  /**
  * Checks if there is some Partner associated with the key
  * @param key Id that we want to check if it has some Partner
  * @return Boolean answering the Question Does this key has a Partner ?
   */
  public boolean hasPartner(String key){
    if (_partners.containsKey(key))
      return true;
    return false;
  }

  /**
  * Checks if there is some Product associated with the key
  * @param key Id that we want to check if it has some Product
  * @return Boolean answering the Question Does this key has a Product ?
   */

  public boolean hasProduct(String key){
    if (_products.containsKey(key))
      return true;
    return false;
  }

  /**
  * Checks if there is some Transaction associated with the key
  * @param key Id that we want to check if it has some Transaction
  * @return Boolean answering the Question Does this key has a Transaction ?
   */

  public boolean hasTransaction(int key){
    if (_transactions.containsKey(key))
      return true;
    return false;
  }
  
  /**
  * Checks if there is some nonePayed associated with the key
  * @param key Id that we want to check if it has some nonePayed
  * @return Boolean answering the Question Does this key has a nonePayed ?
   */

  public boolean nonePayed(int key){
    if (_salesNotPaid.containsKey(key))
      return true;
    return false;
  }

  /**
  * Returns the Partner associated with a key
  * @param key Id of the partner that we want
  * @return Partner associated with the key
   */
  public Partner getPartner(String key){
    Partner p = _partners.get(key);
    return p;
  }

  /**
  * Returns the Product associated with a key
  * @param key Id of the product that we want
  * @return Product associated with the key
   */
  public Product getProduct(String key){
    Product p = _products.get(key);
    return p;
  }

  /**
  * Returns the Batch associated with a key
  * @param key Id of the batch that we want
  * @return Batch associated with the key
   */

  public Batch getBatch(int key){
    Batch b = _batches.get(key);
    return b;
  }

  /**
  * Returns the Transaction associated with a key
  * @param key Id of the transaction that we want
  * @return Transaction associated with the key
   */

  public Transaction getTransaction(int key){
    Transaction t;
    if (_transactions.containsKey(key)){
      t = _transactions.get(key);
    }
    else {
    t = _salesNotPaid.get(key);
    }
    return t;
  }

  /**
  * Returns the TransactionUID
  @return int Transaction UID */

  public int getTransactionUID(){
    return _transactionUID;
  }

  /**
  * Getter of the date
  * @return Int that corresponds to the current date
   */
  public int getDate_aux() {
    return _date;
  }

  /**
  * @return AvailableBalance */

  public Double getAvailable_aux(){
    return _sales - _purchases;
  }

  /**
  * @return All Sales Balance */
  public Double getSum_aux(){
    return (_sales +nonePayedBalance()) - _purchases;
  }

  /**
  * Add Days to the current Date
  * @param n integer that corresponds to the number of Days that we want to advance
  * @return Nothing
   */
  public void addDays(int n){
    _date += n;
  }

  /**
   * @param txtfile filename to be loaded.
   * @throws IOException
   * @throws BadEntryException
   */
  void importFile(String txtfile) throws IOException, BadEntryException, DuplicateUIPartnerException  {

  
    BufferedReader in = new BufferedReader(new FileReader(txtfile));
      String s;
      while ((s = in.readLine()) != null) {
        String line = new String(s.getBytes(), "UTF-8");
        if (line.charAt(0) == '#')
          continue;

        String[] fields = line.split("\\|");
        switch (fields[0]) {
          case "PARTNER" -> registPartAux(fields[1],fields[2],fields[3]);
          case "BATCH_S" -> registBatchS(fields[1],fields[2],Double.parseDouble(fields[3]),Integer.parseInt(fields[4]));
          case "BATCH_M" -> registBatchM(fields[1],fields[2],Double.parseDouble(fields[3]),Integer.parseInt(fields[4]),Double.parseDouble(fields[5]),fields[6]);

        default -> throw new BadEntryException(fields[0]);
        }
      
    } 
  } 
}
   


