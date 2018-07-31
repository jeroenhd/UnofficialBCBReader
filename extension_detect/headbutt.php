<?php
    // Load Zend module for pretty printing
    include_once('vendor/autoload.php');

    // Check if a file exists
    function headbutt($url){
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_NOBODY, true);

        $data = curl_exec($ch);

        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        if ($httpCode < 400){
            return true;
        } else {
            return false;
        }
    }

    // Generate this file as follows:
    // - Open https://bcb.cat/archive
    // - Open the developer console
    // - Execute the following Javascript:
    /*
     JSON.stringify([...document.querySelectorAll(".chapter")].map( (e) =>
               e.querySelector("a").href.match(/c([0-9\.]+?)\//)[1]).map(
                   u => ({
                       chapter : u,
                       links : (
                           [".jpg","@2x.jpg", "@m.jpg", ".png", "@2x.png", "@m.png"].map(
                               s => ({
                                   quality : s,
                                   url : `https://blasto.enterprises/comics/${u}/1${s}`
                                   })
                           ))})));
    */
    // - Copy the output to head.json
    // - Optional: prettify the JSON in your favourite IDE
    $json = json_decode(file_get_contents("head.json"));

    // Set up a fancy table
    $table = new Zend\Text\Table\Table(array('AutoSeperate'=>Zend\Text\Table\Table::AUTO_SEPARATE_HEADER, 'columnWidths' => array(8, 8, 8, 8)));
    $table->appendRow(array('Chapter','Mobile','Desktop', 'Retina'));

    // Loop through the chapters and extract the right rows
    foreach($json as $chapter){
        $c = $chapter->chapter;
        $links = $chapter->links;

        
        $mobile = 'None';
        $retina = 'None';
        $desktop = 'None';

        $qualities = array();
        foreach($links as $link){
            $q = $link->quality;
            $u = $link->url;

            if(headbutt($u)){
                if (strpos($q, "@m") !== FALSE){
                    $mobile = $q;
                } else if (strpos($q, "@2") !== FALSE){
                    $retina = $q;
                } else {
                    $desktop = $q;
                }
            }
        }
        $table->appendRow(array($c, $mobile, $desktop, $retina));
    }

    // Engage the printing of pretties
    echo $table;
?>