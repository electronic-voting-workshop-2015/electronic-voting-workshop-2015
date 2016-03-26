post '/sendVoteFromGui' Dir.chdir("#{RAILS_ROOT}") do
	
	result = system("/" -jar )

end
  


get '/getBBVotes/:race_id' do |n|
    
  if n.to_i != -1
    votes_hash = Votes.where(race_id: n)
  else
  	votes_hash = Votes.all
  end
  
  	votes_hash.to_json(:except => :updated_at)
end


