package de.klotzi111.util.GsonUtil.component;

import static de.klotzi111.util.GsonUtil.GsonUtilTestHelper.getAsJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.klotzi111.util.GsonUtil.exception.JsonObjectExtensionConflictException;

public class GsonComponentExtenderTest {

	@Test
	public void testGsonComponentExtender() throws JsonObjectExtensionConflictException {
		JsonObject json1 = getAsJsonObject(
			"{'knull1':null,'knull2':true,'ko':'keep','k1':'str','k2':['av1','av2'],'k3':{'ok0':'keep','ok1':'ostr','ok2':['oav1','oav2'],'ok3':{'iok1':'iostr1','iok2':'iostr2'}},'k4':[{'aik1':'aistr1'},{'aik1':'aistr2'}],'k5':[[1,2,3],[4,5,6]]}");
		JsonObject json2 = getAsJsonObject(
			"{'knull1':1,'knull2':null,'k1':'rep','k2':['av3'],'k3':{'ok1':'orep','ok2':['oav3'],'ok3':{'iok1':'iorep'},'oknew':'oknewstr'},'k4':[{'aik1':'aistr3'}],'k5':[[7,8,9],{'wk':'wv'}],'knew':'newstr'}");

		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(false, false), ConflictStrategy.PREFER_FIRST_OBJ, json2);
			JsonObject jsonCompare = json1.deepCopy();
			jsonCompare.add("knew", new JsonPrimitive("newstr"));
			assertEquals(jsonCompare, jsonDst);
		}
		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(false, false), ConflictStrategy.NON_NULL_PREFER_FIRST_OBJ, json2);
			JsonObject jsonCompare = json1.deepCopy();
			jsonCompare.add("knew", new JsonPrimitive("newstr"));
			jsonCompare.add("knull1", json2.get("knull1").deepCopy());
			assertEquals(jsonCompare, jsonDst);
		}
		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(false, false), ConflictStrategy.PREFER_SECOND_OBJ, json2);
			JsonObject jsonCompare = getAsJsonObject(
				"{'knull1':1,'knull2':null,'ko':'keep','k1':'rep','k2':['av3'],'k3':{'ok1':'orep','ok2':['oav3'],'ok3':{'iok1':'iorep'},'oknew':'oknewstr'},'k4':[{'aik1':'aistr3'}],'k5':[[7,8,9],{'wk':'wv'}],'knew':'newstr'}");
			assertEquals(jsonCompare, jsonDst);
		}
		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(false, false), ConflictStrategy.NON_NULL_PREFER_SECOND_OBJ, json2);
			JsonObject jsonCompare = getAsJsonObject(
				"{'knull1':1,'knull2':true,'ko':'keep','k1':'rep','k2':['av3'],'k3':{'ok1':'orep','ok2':['oav3'],'ok3':{'iok1':'iorep'},'oknew':'oknewstr'},'k4':[{'aik1':'aistr3'}],'k5':[[7,8,9],{'wk':'wv'}],'knew':'newstr'}");
			assertEquals(jsonCompare, jsonDst);
		}

		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(false, true), ConflictStrategy.PREFER_SECOND_OBJ, json2);
			JsonObject jsonCompare = getAsJsonObject(
				"{'knull1':1,'knull2':null,'ko':'keep','k1':'rep','k2':['av3'],'k3':{'ok0':'keep','ok1':'orep','ok2':['oav3'],'ok3':{'iok1':'iorep','iok2':'iostr2'},'oknew':'oknewstr'},'k4':[{'aik1':'aistr3'}],'k5':[[7,8,9],{'wk':'wv'}],'knew':'newstr'}");
			assertEquals(jsonCompare, jsonDst);
		}

		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(true, false), ConflictStrategy.PREFER_SECOND_OBJ, json2);
			JsonObject jsonCompare = getAsJsonObject(
				"{'knull1':1,'knull2':null,'ko':'keep','k1':'rep','k2':['av1','av2','av3'],'k3':{'ok1':'orep','ok2':['oav3'],'ok3':{'iok1':'iorep'},'oknew':'oknewstr'},'k4':[{'aik1':'aistr1'},{'aik1':'aistr2'},{'aik1':'aistr3'}],'k5':[[1,2,3],[4,5,6],[7,8,9],{'wk':'wv'}],'knew':'newstr'}");
			assertEquals(jsonCompare, jsonDst);
		}

		{
			JsonObject jsonDst = json1.deepCopy();
			GsonComponentExtender.extendJsonObject(jsonDst, new MergeStrategy(true, true), ConflictStrategy.PREFER_SECOND_OBJ, json2);
			JsonObject jsonCompare = getAsJsonObject(
				"{'knull1':1,'knull2':null,'ko':'keep','k1':'rep','k2':['av1','av2','av3'],'k3':{'ok0':'keep','ok1':'orep','ok2':['oav1','oav2','oav3'],'ok3':{'iok1':'iorep','iok2':'iostr2'},'oknew':'oknewstr'},'k4':[{'aik1':'aistr1'},{'aik1':'aistr2'},{'aik1':'aistr3'}],'k5':[[1,2,3],[4,5,6],[7,8,9],{'wk':'wv'}],'knew':'newstr'}");
			assertEquals(jsonCompare, jsonDst);
		}
	}

}
