package com.kxf.test2;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;

import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransactionClient {

	private static String fromAddress = "0x897081cB98b838b4fFF8A1FcEeC4faD188CA9281";
	private static String toAddress = "0x58DB40b3716D8eA0339B564F085434982DF322AB";

	private static String fromPrivateKey = "40dc5ccc231d999900d5fd509ecb53412a7e7eeb71eb7dc941585c3675537d2f";
	private static String conaddress = "0xc54083e77f913a4f99e1232ae80c318ff03c9d17";
	private static BigDecimal defaultGasPrice = BigDecimal.valueOf(1);///
	// eth转账 gasPrice =gasLimit (21000) * (1-60) / 10^9
	// 普通转账 gasPrice= gasLimit(60000) *(1-60)/10^9

	public static void main(String[] args) {
		selectCoin();
	}

	public static BigInteger getGasLimit(BigInteger amountint) {
		StringBuilder url = new StringBuilder();
		url.append("https://api.etherscan.io/api?module=proxy&action=eth_estimateGas&to=");
		url.append(toAddress).append("&value=").append(amountint).append("&gasPrice=0&gas=0");
		try {
			String result = SimpleHttpUtils.get(url.toString());
			System.out.println(result);
			JSONObject json = JSONObject.parseObject(result);
			String jsonResult = json.getString("result");
			if (jsonResult == null) {
				return null;
			}
			BigInteger bigInteger = Numeric.decodeQuantity(jsonResult);
			System.err.println(bigInteger);
			return bigInteger;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}

	}

	public static BigInteger getNoce(String address) {
		String url = String.format(
				"https://api.etherscan.io/api?module=proxy&action=eth_getTransactionCount&address=%s&tag=latest",
				address);
		try {
			String result = SimpleHttpUtils.get(url.toString());
			JSONObject json = JSONObject.parseObject(result);
			String jsonResult = json.getString("result");
			if (jsonResult == null) {
				return null;
			}
			BigInteger bigInteger = Numeric.decodeQuantity(jsonResult);

			return bigInteger;
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}

	}

	/**
	 * 签名交易
	 */
	public static String signRawTransaction(RawTransaction rawTransaction, byte chainId, String privateKey)
			throws IOException {
		byte[] signedMessage;
		if (privateKey.startsWith("0x")) {
			privateKey = privateKey.substring(2);
		}
		ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
		Credentials credentials = Credentials.create(ecKeyPair);

		if (chainId > ChainId.NONE) {
			signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
		} else {
			signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		}
		String hexValue = Numeric.toHexString(signedMessage);
		return hexValue;
	}

	public static void sendETHRawTransaction() {
		BigDecimal amount = new BigDecimal("0.0033");
		// {"jsonrpc":"2.0","id":1,"result":"0xe7be3828833cf3d7a66869e0ee9250eee5c6172deb856c13871350cb1ddfc6a0"}
		BigInteger amountint = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
		BigInteger gasLimit = getGasLimit(amountint);
		BigInteger nonce = getNoce(fromAddress);
		byte chainId = ChainId.NONE;
		BigInteger gasPrice = Convert.toWei(defaultGasPrice, Convert.Unit.GWEI).toBigInteger();// eth
		RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress,
				amountint);
		try {
			System.err.println(JSON.toJSONString(rawTransaction).toString());
			String sinData = signRawTransaction(rawTransaction, chainId, fromPrivateKey);
			System.err.println("sinData:" + sinData);
			String url = String.format("https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=%s",
					sinData);
			String body = SimpleHttpUtils.get(url);
			System.out.println(body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 133,200
	// 0x11bb0c03c3fc357c1ebbc447cc054795beac0c3eed5527b4fdf9041cdaa6b02f
	public static void sendOtherRawTransaction() {
		BigDecimal amount = new BigDecimal("8.888");
		BigInteger amountint = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
		List<Type> inputParameters = new ArrayList<>();
		List<TypeReference<?>> outputParameters = new ArrayList<>();
		Address tAddress = new Address(toAddress);
		Uint256 tokenValue = new Uint256(amountint);
		inputParameters.add(tAddress);
		inputParameters.add(tokenValue);
		TypeReference<Bool> typeReference = new TypeReference<Bool>() {
		};
		outputParameters.add(typeReference);
		String methodName = "transfer";
		Function function = new Function(methodName, inputParameters, outputParameters);
		String data = FunctionEncoder.encode(function);
		System.err.println(data);
		// 0xa9059cbb00000000000000000000000058db40b3716d8ea0339b564f085434982df322ab0000000000000000000000000000000000000000000000007b5884e8cbac0000
		parseInput(data);
		BigInteger gasLimit = new BigInteger("60000");
		BigInteger nonce = getNoce(fromAddress);
		byte chainId = ChainId.NONE;
		BigInteger gasPrice = Convert.toWei(new BigDecimal("2.22"), Convert.Unit.GWEI).toBigInteger();// eth
		RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, conaddress, data);
		try {
			System.err.println(JSON.toJSONString(rawTransaction).toString());
			String sinData = signRawTransaction(rawTransaction, chainId, fromPrivateKey);
			System.err.println("sinData:" + sinData);
			String url = String.format("https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=%s",
					sinData);
			String body = SimpleHttpUtils.get(url);
			System.out.println(body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parseInput(String inputData) {
		String method = inputData.substring(0, 10);
		String to = inputData.substring(10, 74);
		String value = inputData.substring(74);
		Method refMethod = null;
		try {
			refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		refMethod.setAccessible(true);
		Address address = null;
		try {
			address = (Address) refMethod.invoke(null, to, 0, Address.class);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		Uint256 amount = null;
		try {
			amount = (Uint256) refMethod.invoke(null, value, 0, Uint256.class);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		System.out.println("method=" + method + "\nto=" + to + "\nvalue=" + value + "\naddress=" + address.getValue()
				+ "\namount=" + amount.getValue());
	}

	////////// 获取交易信息
	public static void getTransfer() {
		String url = "https://api.etherscan.io/api?module=account&action=tokentx&contractaddress=0xc54083e77f913a4f99e1232ae80c318ff03c9d17&address=0x897081cb98b838b4fff8a1fceec4fad188ca9281&page=1&offset=4&sort=desc";
		try {
			String backstr = SimpleHttpUtils.get(url);
			JSONObject backjson = JSONObject.parseObject(backstr);
			JSONArray jsonArray = backjson.getJSONArray("result");
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				TransactionsEntity entity = JSONObject.parseObject(jsonObject.toJSONString(), TransactionsEntity.class);
				System.err.println(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	  $.ajax({
//          dataType: "json",
//          type: 'Get',
//          url: '/searchHandler?t=t',
//          data: { term: searchTerm },
//          success: function (data) {
//              response($.map(data, function (item) {
//                  return {
//                      label: item.split('\t')[0],
//                      value: item.split('\t')[1],
//                      desc: item.split('\t')[2],
//                      typeval: item.split('\t')[3],
//                      checkMark: item.split('\t')[4],
//                      logo: item.split('\t')[5]
//                  }
//              }));
//          },
//          error: function (XMLHttpRequest, textStatus, errorThrown) {
//              console.log(textStatus);
//          }
//      });
	// 查询代币或合约资产
	// "ZRX (ZRX)\t0xe41d2489571d322189246dafa5ebde1f4699f498\tTOKEN :
	// 0xe41d2489571d322189246dafa5e...\u003cbr\u003ehttps://0xproject.com/\t2\t1\t0xtoken_28.png"
	public static void selectCoin() {
		System.out.println("输入搜索名称");
		Scanner scan = new Scanner(System.in);
		String read = scan.nextLine();
		String url = "https://etherscan.io/searchHandler?t=t&term=" + read;
		try {
			String backstr = SimpleHttpUtils.get(url);
			JSONArray jsonArray = JSONArray.parseArray(backstr);
			for (int i = 0; i < jsonArray.size(); i++) {
				String backJson = jsonArray.getString(i);
				int tlenght = backJson.split("\t").length;
				String label = null,value = null,desc = null,typeval = null,checkMark = null,logo = null;
				if (tlenght > 0) {
					 label = backJson.split("\t")[0];
				}
				if (tlenght > 1) {
					 value = backJson.split("\t")[1];
				}
				if (tlenght > 2) {
					 desc = backJson.split("\t")[2];
				}
				if (tlenght > 3) {
					 typeval = backJson.split("\t")[3];
				}
				if (tlenght > 4) {
					 checkMark = backJson.split("\t")[4];
				}
				if (tlenght > 5) {
					 logo = backJson.split("\t")[5];
				}
				System.out.println("label:" + label + " value: " + value + "  desc:" + desc + " typeval:" + typeval
						+ " checkMark:" + checkMark + " logo: " + logo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
