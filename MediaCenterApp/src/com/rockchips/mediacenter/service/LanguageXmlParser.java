package com.rockchips.mediacenter.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LanguageXmlParser
{
    private List<ModelLanguage> mModelLanguageList;

    private String language;

    public LanguageXmlParser()
    {
        initLanguageRes();
        parseXml();
    }

    private void initLanguageRes()
    {

        language = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "				<language id=\"1\" >" + "				<nation " + "				abb=\"zho\"" + "lang=\"Chinese\" />" + "				<nation" + " 				abb=\"chi\"" + " 				lang=\"Chinese\" />" + "				<nation"
                + " 				abb=\"eng\"" + "  				lang=\"English\" />" + "				<nation" + " 				abb=\"jpn\"" + " 				lang=\"Japanese\" />" + "				<nation" + "    		        abb=\"deu\"" + "    		        lang=\"German\" />" + "    		    <nation"
                + "    		        abb=\"fra\"" + "    		        lang=\"French\" />" + "    		    <nation" + "    		        abb=\"ita\"" + "    		        lang=\"Italian\" />" + "    		    <nation" + "    		        abb=\"nld\""
                + "    		        lang=\"Dutch\" />" + "    		    <nation" + "    		        abb=\"tur\"" + "    		        lang=\"Turkish\" />" + "    		    <nation" + "    		        abb=\"ind\"" + "    		        lang=\"Indonesian\" />"
                + "    		    <nation" + "    		        abb=\"hin\"" + "    		        lang=\"Hindi\" />" + "    		    <nation" + "    		        abb=\"kor\"" + "    		        lang=\"Korean\" />" + "    		    <nation" + "    		        abb=\"tha\""
                + "    		        lang=\"Thai\" />" + "    		    <nation" + "    		        abb=\"spa\"" + "    		        lang=\"Spanish\" />" + "    		    <nation" + "    		        abb=\"por\"" + "    		        lang=\"Portuguese\" />"
                + "    		    <nation" + "    		        abb=\"swe\"" + "    		        lang=\"Swedish\" />" + "    		    <nation" + "    		        abb=\"rus\"" + "    		        lang=\"Russian\" />" + "    		    <nation" + "    		        abb=\"cat\""
                + "    		        lang=\"Catalan\" />" + "    		    <nation" + "    		        abb=\"dan\"" + "    		        lang=\"Danish\" />" + "    		    <nation" + "    		        abb=\"fin\"" + "    		        lang=\"Finnish\" />"
                + "    		    <nation" + "    		        abb=\"ell\"" + "    		        lang=\"Greek\" />" + "    		    <nation" + "    		        abb=\"isl\"" + "    		        lang=\"Icelandic\" />" + "    		    <nation" + "    		        abb=\"nor\""
                + "    		        lang=\"Norwegian\" />" + "    		    <nation" + "    		        abb=\"ara\"" + "    		        lang=\"Arabic\" />" + "    		    <nation" + "    		        abb=\"bul\"" + "    		        lang=\"Bulgarian\" />"
                + "    		    <nation" + "    		        abb=\"hrv\"" + "    		        lang=\"Croatian\" />" + "    		    <nation" + "    		        abb=\"ces\"" + "    		        lang=\"Czech\" />" + "    		    <nation" + "    		        abb=\"est\""
                + "    		        lang=\"Estonian\" />" + "    		    <nation" + "    		        abb=\"heb\"" + "    		        lang=\"Hebrew\" />" + "    		    <nation" + "    		        abb=\"hun\"" + "    		        lang=\"Hungarian\" />"
                + "    		    <nation" + "    		        abb=\"lav\"" + "    		        lang=\"Latvian\" />" + "    		    <nation" + "    		        abb=\"lit\"" + "    		        lang=\"Lithuanian\" />" + "    		    <nation" + "    		        abb=\"pol\""
                + "    		        lang=\"Polish\" />" + "    		    <nation" + "    		        abb=\"ron\"" + "    		        lang=\"Romanian\" />" + "    		    <nation" + "    		        abb=\"srp\"" + "    		        lang=\"Serbian\" />"
                + "    		    <nation" + "    		        abb=\"slv\"" + "    		        lang=\"Slovenian\" />" + "    		    <nation" + "    		        abb=\"ukr\"" + "    		        lang=\"Ukrainian\" />" + "    		    <nation"
                + "    		        abb=\"slk\"" + "    		        lang=\"Slovak\" />" + "    		    <nation" + "    		        abb=\"sla\"" + "    		        lang=\"Slovak\" />" + "    		    <nation" + "    		        abb=\"tam\""
                + "    		        lang=\"Tamil\" />" + "    		    <nation" + "    		        abb=\"tel\"" + "    		        lang=\"Telugu\" />" + "    		    <nation" + "    		        abb=\"vie\"" + "    		        lang=\"Vietnamese\" />"
                + "    		    <nation" + "    		        abb=\"rom\"" + "    		        lang=\"Romanian\" />" + "    		    <nation" + "    		        abb=\"tgl\"" + "    		        lang=\"Filipino\" />" + "    		    <nation" + "    		        abb=\"msa\""
                + "    		        lang=\"Indonesian\" />" + "    		</language>";
    }

    private void parseXml()
    {
        mModelLanguageList = new ArrayList<ModelLanguage>();

        InputStream _InputStream = new ByteArrayInputStream(language.getBytes());

        DocumentBuilderFactory _Factory = null;
        DocumentBuilder _Builder = null;
        Document _Doc = null;

        try
        {
            _Factory = DocumentBuilderFactory.newInstance();
            _Builder = _Factory.newDocumentBuilder();
            _Doc = _Builder.parse(_InputStream);
        }
        catch (ParserConfigurationException e)
        {
        }
        catch (SAXException e)
        {
        }
        catch (IOException e)
        {
        }
        finally
        {
            try
            {
                _InputStream.close();
            }
            catch (IOException e)
            {
            }
            _InputStream = null;
        }
        
        if (null == _Doc)
        {
            return;
        }

        Element _Element = _Doc.getElementById("1");
        NodeList _NodeList = _Element.getChildNodes();

        for (int i = 0; i < _NodeList.getLength(); i++)
        {
            Node _Node = _NodeList.item(i);

            if (_Node.getNodeName().equals("nation"))
            {
                NamedNodeMap _NamedNodeMap = _Node.getAttributes();
                mModelLanguageList.add(new ModelLanguage(_NamedNodeMap.item(0).getNodeValue(), _NamedNodeMap.item(1).getNodeValue()));
            }
        }
    }

    public String getLanguage(String pAbb)
    {
        String _Lang = pAbb;

        if (!mModelLanguageList.isEmpty())
        {
            for (ModelLanguage _ModelLanguage : mModelLanguageList)
            {
                if (_ModelLanguage.getAbb().equals(pAbb))
                {
                    _Lang = _ModelLanguage.getLang();
                    break;
                }
            }
        }

        return _Lang;
    }
}
