package com.grail.synchro.beans;

import com.jive.api.entitlement.impl.ApiEntitlementImpl;

/**
 * @author Tejinder
 * @version 1.0
 */
public class ResearchPONum extends BeanObject {
	private String budgetApproverId;
	private String poNum;
	private boolean poCheckBox;
	public String getBudgetApproverId() {
		return budgetApproverId;
	}
	public void setBudgetApproverId(String budgetApproverId) {
		this.budgetApproverId = budgetApproverId;
	}
	public String getPoNum() {
		return poNum;
	}
	public void setPoNum(String poNum) {
		this.poNum = poNum;
	}
	public boolean isPoCheckBox() {
		return poCheckBox;
	}
	public void setPoCheckBox(boolean poCheckBox) {
		this.poCheckBox = poCheckBox;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResearchPONum)) {
            return false;
        }
       
        ResearchPONum that = (ResearchPONum) o;

        if (budgetApproverId != that.budgetApproverId) {
            return false;
        }
        

        return true;
    }

    @Override
    public int hashCode() {
        
        return Integer.valueOf(budgetApproverId);
    }

	
}
