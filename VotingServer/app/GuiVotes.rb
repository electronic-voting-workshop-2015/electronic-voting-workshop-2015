post '/vote'  do
	
	result = system("java -jar sadna.jar vote #{request}")
#	result.to_json

end

post '/audit'  do
	
	result = system("java -jar sadna.jar audit #{request}")
#	result.to_json

end
  


