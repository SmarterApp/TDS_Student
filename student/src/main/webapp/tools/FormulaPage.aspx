<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="FormulaPage.aspx.cs" Inherits="TDS.Student.Web.UI.Tools.FormulaPage" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >

    <head runat="server">
        <title>Formulas</title>
        <tds:CSSLink runat="server" href="~/shared/css/testShell.css" media="screen" type="text/css" rel="stylesheet" />
	    <tds:CSSLink runat="server" href="~/projects/{0}/css/testShell.css" rel="stylesheet" type="text/css" />
    </head>

    <body id="htmlBody" runat="server">
        <div class="popupPage">
            <!-- oregon's formulas -->
            <div class="formulaHolder or_grade_3_5_math"></div>
            <div class="formulaHolder or_grade_6_8_math"></div>
            <div class="formulaHolder or_grade_10_math"></div>
            
            <div class="formulaHolder hi_grade_4_math"></div>
            <div class="formulaHolder hi_grade_5_math"></div>
            <div class="formulaHolder hi_grade_6_math"></div>
            <div class="formulaHolder hi_grade_7_math"></div>
            <div class="formulaHolder hi_grade_8_math"></div>
            <div class="formulaHolder hi_grade_10_math"></div>
            <div class="formulaHolder hi_grade_11_science"></div>
            
           	<div class="formulaHolder de_grade_6_8_math"></div>
            <div class="formulaHolder de_grade_9_10_math"></div>
        </div>
    </body>

</html>
