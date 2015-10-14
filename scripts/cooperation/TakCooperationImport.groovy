#!/usr/bin/env groovy

/**
 * Imports TAK data from TAK export JSON to cooperation database
 *
 * 1, Start your local H2 database and make sure cooperation database and tables exist
 * 2, Make sure you have exported data from TAK using TakCooperationExport.groovy
 * 3, Update the database config in this script
 * 4, Run this groovy script by $groovy TakCooperationImport.groovy > importLog.txt
 */

@Grapes([
	@GrabConfig(systemClassLoader=true),
	@Grab(group='com.h2database', module='h2', version='1.4.187'),
	@Grab(group='org.hsqldb', module='hsqldb', version='2.3.3'),
	@Grab(group='mysql', module='mysql-connector-java', version='5.1.36')
])

import groovy.io.FileType
import groovy.json.JsonSlurper
import groovy.sql.Sql

def countRows = { description, table ->
	def result = db.firstRow("SELECT COUNT(*) AS numberOfRows FROM " + table)
	println "$table, $description, rows: $result.numberOfRows"
}

def connectionPoint(db, platform, environment, exportTime){
	if(db.firstRow("SELECT * FROM connectionpoint_new WHERE platform = $platform AND environment = $environment") == null){
		db.executeInsert "insert into connectionpoint_new(platform, environment, snapshot_time)  values($platform, $environment, $exportTime)"
	}else{
		println "INFO: Connectionpoint platform: $platform, environment: $environment already exist"
	}
}

def logicalAddress(db, inputJSON){
	inputJSON.data.logiskadress.each{
		if(db.firstRow("SELECT * FROM logicaladdress_new WHERE logical_address = $it.hsaId") == null){
			db.executeInsert "insert into logicaladdress_new(logical_address,description)  values($it.hsaId, $it.beskrivning)"
		}else{
			println "INFO: Logical address $it already exist"
		}
	}
}

def serviceContract(db, inputJSON){
	inputJSON.data.tjanstekontrakt.each{

		def domain = domain(it.namnrymd)
		if(db.firstRow("SELECT * FROM servicecontract_new WHERE namespace = $it.namnrymd") == null){
			db.executeInsert "insert into servicecontract_new(major,minor,name, namespace, service_domain_id) \
                    select $it.majorVersion, $it.minorVersion, $it.beskrivning, $it.namnrymd, c.id from \
                    (SELECT id FROM servicedomain_new WHERE namespace = $domain ) as c"
		}else{
			println "INFO: Servicecontract $it already exist"
		}
	}
}

def serviceDomain(db, inputJSON){
	inputJSON.data.tjanstekontrakt.each{

		def domainrymd = domain(it.namnrymd)
		if(db.firstRow("SELECT * FROM servicedomain_new WHERE namespace = $domainrymd") == null){
			db.executeInsert "insert into servicedomain_new(name, namespace)  values('namn', $domainrymd)"
		}else{
			println "INFO: Servicedomain $it already exist"
		}
	}
}

def serviceConsumer(db, inputJSON){
	inputJSON.data.tjanstekonsument.each{

		if(db.firstRow("SELECT * FROM serviceconsumer_new WHERE hsa_id = $it.hsaId") == null){
			db.executeInsert "insert into serviceconsumer_new(hsa_id, description)  values($it.hsaId, $it.beskrivning)"
		}else{
			println "INFO: Serviceconsumer $it already exist"
		}
	}
}

def serviceProducer(db, inputJSON){
	inputJSON.data.tjansteproducent.each{

		if(db.firstRow("SELECT * FROM serviceproducer_new WHERE hsa_id = $it.hsaId") == null){
			db.executeInsert "insert into serviceproducer_new(hsa_id, description)  values($it.hsaId, $it.beskrivning)"
		}else{
			println "INFO: Serviceproducer $it already exist"
		}
	}
}

def cooperation(db, inputJSON, platform, environment){
	inputJSON.data.anropsbehorighet.each{

		if(db.firstRow(
			"SELECT * FROM cooperation_new c, logicaladdress_new l, serviceconsumer_new s, servicecontract_new sc, connectionpoint_new cp \
                WHERE c.logical_address_id = l.id \
                AND c.service_consumer_id = s.id \
                AND c.service_contract_id = sc.id \
                AND c.connection_point_id = cp.id \
                AND l.logical_address = $it.relationships.logiskAdress \
                AND s.hsa_id = $it.relationships.tjanstekonsument \
                AND sc.namespace = $it.relationships.tjanstekontrakt \
                AND cp.environment = $environment \
                AND cp.platform = $platform") == null){

			db.executeInsert \
                "insert into cooperation_new(connection_point_id, logical_address_id, service_consumer_id, service_contract_id) \
                    select c.id, address.id, consumer.id, contract.id \
                    from \
                        (SELECT id FROM connectionpoint_new WHERE platform = $platform AND environment = $environment) as c, \
                        (SELECT id FROM logicaladdress_new WHERE logical_address = $it.relationships.logiskAdress) as address, \
                        (SELECT id FROM serviceconsumer_new WHERE hsa_id = $it.relationships.tjanstekonsument) as consumer,\
                        (SELECT id FROM servicecontract_new WHERE namespace = $it.relationships.tjanstekontrakt) as contract"
		}else{
			println "INFO: Cooperation for serviceconsumer $it already exist"
		}
	}
}

def serviceProduction(db, inputJSON, platform, environment){
	inputJSON.data.vagval.each{

		if(db.firstRow(
			"SELECT * FROM serviceproduction_new c, logicaladdress_new l, serviceproducer_new s, servicecontract_new sc, connectionpoint_new cp \
                WHERE c.logical_address_id = l.id \
                AND c.service_producer_id = s.id \
                AND c.service_contract_id = sc.id \
                AND c.connection_point_id = cp.id \
                AND l.logical_address = $it.relationships.logiskadress \
                AND s.hsa_id = $it.relationships.tjansteproducent \
                AND sc.namespace = $it.relationships.tjanstekontrakt \
                AND cp.environment = $environment \
                AND cp.platform = $platform") == null){

			db.executeInsert \
                "insert into serviceproduction_new(physical_address, rivta_profile, connection_point_id, logical_address_id, service_producer_id, service_contract_id) \
                    select $it.relationships.anropsadress, $it.relationships.rivtaProfil, c.id, address.id, producer.id, contract.id \
                    from \
                        (SELECT id FROM connectionpoint_new WHERE platform = $platform AND environment = $environment) as c, \
                        (SELECT id FROM logicaladdress_new WHERE logical_address = $it.relationships.logiskadress) as address, \
                        (SELECT id FROM serviceproducer_new WHERE hsa_id = $it.relationships.tjansteproducent) as producer,\
                        (SELECT id FROM servicecontract_new WHERE namespace = $it.relationships.tjanstekontrakt) as contract"
		}else{
			println "INFO: Serviceproduction already exist $it"
		}
	}
}

def domain(namespace){

	def temp = namespace.replaceFirst('urn:riv:', '')
	def domain = temp.split(':[A-Z]')[0]
	return domain
}


def clearDatabase(db) {

	println ''
	println 'START! Clearing database'
	println ''
	db.execute 'SET REFERENTIAL_INTEGRITY FALSE'
	db.execute "delete from connectionpoint"
	db.execute "delete from cooperation"
	db.execute "delete from logicaladdress"
	db.execute "delete from serviceconsumer"
	db.execute "delete from servicecontract"
	db.execute "delete from serviceproducer"
	db.execute "delete from serviceproduction"
	db.execute "delete from servicedomain"
	db.execute 'SET REFERENTIAL_INTEGRITY TRUE'
	println ''
	println 'Database is cleared'
	println ''


}

def cli = new CliBuilder(
	usage: 'TakCooperationImport [options]',
	header: '\nAvailable options (use -h for help):\n')
cli.with
	{
		h longOpt: 'help', 'Usage Information', required: false
		_ longOpt: 'url', 'Connection URL \n eg. jdbc:h2:tcp://localhost/~/cooperation', args: 1, required: true
		u longOpt: 'user', 'User ID', args: 1, required: true
		p longOpt: 'password', 'Password', args: 1, required: false
		_ longOpt: 'clear', 'Clear database before importing', required: false
		d longOpt: 'directory', 'Directory that holds data dump files', args:1, required: false
	}

def opt = cli.parse(args)
if (!opt) return
if (opt.h) cli.usage()

def url = opt.url
def username = opt.u
def password = opt.p ? opt.p : ''
def dataDirectory = opt.d ? opt.d.replaceFirst("^~",System.getProperty("user.home")) : '.'

//Cooperation db settings
def db = Sql.newInstance(url, username, password, 'com.mysql.jdbc.Driver')

if (opt.clear) clearDatabase(db)

println """\
  START! Importing all tak data to cooperation database

  Options: $opt

"""

//Import all json files in current directory
def directory = new File(dataDirectory)
directory.eachFileMatch(FileType.FILES, ~/.*json/) {

	//Extract env and platform from file name with convention takdump_platform_environment.json
	def fileName = it.name.replaceFirst(~/\.[^\.]+$/, '')
	def platform = fileName.split('_')[1].toUpperCase()
	def environment = fileName.split('_')[2].toUpperCase()

	def inputJSON = new JsonSlurper().parseText(it.text)

	println "****** START IMPORT FILE $it.name ******************************************************"
	println 'Timestamp starting: ' + new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
	println "Format version: $inputJSON.formatVersion"
	println "Description: $inputJSON.beskrivning"
	println "Timestamp of exported TAK data: $inputJSON.tidpunkt"
	println "Import from platform: $platform and environment: $environment"
	println '************************************************************'

	connectionPoint(db, platform, environment, inputJSON.tidpunkt)
	logicalAddress(db, inputJSON)
	serviceDomain(db, inputJSON)
	serviceContract(db, inputJSON)
	serviceConsumer(db, inputJSON)
	serviceProducer(db, inputJSON)
	cooperation(db, inputJSON, platform, environment)
	serviceProduction(db, inputJSON, platform, environment)

	println "******* END IMPORT FILE $it.name *****************************************************"
	println 'Timestamp finishing: ' + new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
	println '************************************************************'
}

db.close();

println ''
println 'DONE! Import all tak data to cooperation database'
println ''
