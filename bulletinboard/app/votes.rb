post '/sendVote' do

  #todo: verify signature
  
  for index in 0 ... params['votes'].size-1
    create = votes.create(vote_value: params['votes'].fetch(index)['vote_value'],
                          ballot_box: params['ballot_box'].to_i,
                          serial_number: params['serial_number'].to_i,
                          race_id: index+1)
  end
  
end

get '/getBBVotes' do
  
  input_race_id = parmas['race_id']    
    
  if race_id != -1
    votes_hash = votes.where(race_id: input_race_id)
  else
    votes_hash = votes.find(:all)
  
  votes_hash.to_json(:except => :updated_at)
end

post '/publishZKP' do
	begin
  
		#todo: verify signature
  
		voteZkp = ZKP.find_by(vote_id: params['vote_id'])
		voteZkp.update("party_#{params[:party_id]}": params['zkp'])
  
		rescue ActiveRecord::RecordNotFound
			create = ZKP.create(vote_id: params['vote_id'].to_i, "party_#{params['party_id']}": params['zkp'])
		end
	end
end
