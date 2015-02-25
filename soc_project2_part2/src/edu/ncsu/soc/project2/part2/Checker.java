package edu.ncsu.soc.project2.part2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.util.FileManager;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Checker extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checker);
		TextView tView = (TextView) findViewById(R.id.output_message);
		String output = "";
		Model schema = ModelFactory.createDefaultModel();//FileManager.get().loadModel("file:assets/PO_new.owl");
		Model model = ModelFactory.createDefaultModel();//FileManager.get().loadModel("file:assets/instance.rdf");
		InputStream fin = null;
		tView.setMovementMethod(new ScrollingMovementMethod());
		
		try {
			AssetManager assetManager = getAssets();
			fin = (InputStream) assetManager.open("PO_new.owl");
			schema.read(fin,null);
			fin.close();

		/*
			1. 2Item_Instance.rdf - Has two distinct "hasItem" properties with each item explicitly stated different to each other.
			2. 2Orders_Instance.rdf - Has two "OrderNumber" properties under PurchaseOrder.
			3. normal_instance.rdf - Working instance of ontology PO_new.owl
			4. PO_new.owl - contains Purchase Order Ontology.
			5. StringOrder_Instance - Has a string instead of Integer as Order number.
		*/
			
			model = FileManager.get().loadModel("assets/normal_instance.rdf");
//			model = FileManager.get().loadModel("assets/2Orders_Instance.rdf");
//			model = FileManager.get().loadModel("assets/2Item_Instance.rdf");
//			model = FileManager.get().loadModel("assets/StringOrder_Instance.rdf");
		} catch (IOException e){
			e.printStackTrace();
		}
		
		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
		reasoner = reasoner.bindSchema(schema);

		InfModel infmodel = ModelFactory.createInfModel(reasoner, model);
		ValidityReport validity = infmodel.validate();
		if (validity.isValid()) {
			output += "Order Confirmed\n";
			output = getInfo(infmodel, model, output);
		} else {
			for (Iterator<Report> i = validity.getReports(); i.hasNext();) {
				ValidityReport.Report report = (ValidityReport.Report) i.next();
				output += "ERROR: " + report.getType(); 
				output += " - " + report.getDescription();
			}
		}
		tView.setText(output);
	}

	String getInfo(InfModel infmodel, Model data, String output) {
		StmtIterator iter = data.listStatements();
		float unitPrice = 0;
		int quantity = 0;
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			// Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			String propertyValue = stmt.getObject().toString();
			if(propertyValue.indexOf('^') != -1 )
				propertyValue = propertyValue.substring(0,propertyValue.indexOf('^'));
			if (predicate.toString().contains("OrderNumber"))
				output += "OrderNumber = " + propertyValue + "\n";
			else if (predicate.toString().contains("OrderDate"))
				output += "OrderDate = " + propertyValue + "\n";
			else if (predicate.toString().contains("FirstName"))
				output += "FirstName = " + propertyValue + "\n";
			else if (predicate.toString().contains("LastName"))
				output += "LastName = " + propertyValue + "\n";
			else if (predicate.toString().contains("ItemName"))
				output += "ItemName = " + propertyValue + "\n";
			else if (predicate.toString().contains("Quantity")) {
				output += "Quantity = " + propertyValue + "\n";
				quantity = Integer.valueOf(propertyValue);
			} else if (predicate.toString().contains("shippingAddress"))
				output += "ShippingAddress: " + "\n";
			else if(predicate.toString().contains("Street"))
				output += "\t Street = " + propertyValue + "\n";
			else if(predicate.toString().contains("City"))
				output += "\t City = " + propertyValue + "\n";
			else if(predicate.toString().contains("State"))
				output += "\t State = " + propertyValue + "\n";
			else if(predicate.toString().contains("Zip"))
				output += "\t Zip = " + propertyValue + "\n";
			else if (predicate.toString().contains("unitPrice"))
				unitPrice = Float.valueOf(propertyValue);
		}
		output += "TotalPrice = " + quantity * unitPrice + "\n";
		return output;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.checker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
