package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import pt.tecnico.uilib.forms.Form;
import ggc.app.exceptions.UnknownPartnerKeyException;
import ggc.exceptions.UnknownPartnerException;
import java.util.LinkedHashMap;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Register order.
 */
public class DoRegisterAcquisitionTransaction extends Command<WarehouseManager> {

  public DoRegisterAcquisitionTransaction(WarehouseManager receiver) {
    super(Label.REGISTER_ACQUISITION_TRANSACTION, receiver);
    addStringField("partnerId",Prompt.partnerKey());
    addStringField("productId",Prompt.productKey());
    addRealField("price",Prompt.price());
    addIntegerField("amount",Prompt.amount());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      String partnerId = stringField("partnerId");
      String productId = stringField("productId");
      Double price = realField("price");
      int amount = integerField("amount");
      if (_receiver.productVerification(productId)){
        _receiver.registeAcquisition(partnerId,productId,price,amount);
      }
      else {
        String confirm = Form.requestString(Prompt.addRecipe());
        if (confirm.equals("n") || confirm.equals("nao")){
          _receiver.registeAcquisition(partnerId,productId,price,amount);
        }
        if (confirm.equals("s") || confirm.equals("sim")){
          int n_components = Form.requestInteger(Prompt.numberOfComponents());
          Double alpha = Form.requestReal(Prompt.alpha());
          LinkedHashMap<String,Integer> recipe = new LinkedHashMap<String,Integer>();
          while (n_components > 0){
            String productIdRecipe = Form.requestString(Prompt.productKey());
            int productAmount = Form.requestInteger(Prompt.amount());
            n_components -= 1;
            recipe.put(productIdRecipe,productAmount);
          }
          String recipeString = _receiver.recipeToString(recipe);
          recipeString = recipeString.substring(0, recipeString.length() -1);
          _receiver.registBatchBuyM(productId,partnerId,price,amount,alpha,recipeString);
          _receiver.registeAcquisition(productId,partnerId,price,amount);
        }
      }
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
  }

}
