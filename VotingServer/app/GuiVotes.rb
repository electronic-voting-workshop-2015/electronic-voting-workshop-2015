post '/sendVoteFromGui'  do
	
	result = system("java -jar sadna.jar #{request}")
#	result.to_json

end
  


