

options '/Vote'  do

#	puts "hello"
	result = system("java -jar sadna.jar vote #{request}")
#	result.to_json

end

options '/Audit'  do
	
	result = system("java -jar sadna.jar audit #{request}")
#	result.to_json

end
  


