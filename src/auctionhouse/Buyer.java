package auctionhouse;

public class Buyer {

	private String name;
	private String address;
	private String bankAccount;
	private String bankAuthCode;

	public Buyer(String name, String address, String bankAccount, String bankAuthCode) {

		this.name = name;
		this.address = address;
		this.bankAccount = bankAccount;
		this.bankAuthCode = bankAuthCode;
	}

	/*
	 * public void setName(String name) { this.name = name; }
	 * 
	 * public void setAddress(String address) { this.address = address; }
	 * 
	 * public void setBankAccount(String bankAccount) { this.bankAccount =
	 * bankAccount; }
	 * 
	 * public void setBankAuthCode(String bankAuthCode) { this.bankAuthCode =
	 * bankAuthCode; }
	 */
	public String getBankAccount() {
		return bankAccount;
	}

	public String getBankAuthCode() {
		return bankAuthCode;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}
}
