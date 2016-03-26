require 'models/votes'
require 'models/ZKP'

post '/sendVote' do

	_party_id = params['ballot_box'].to_i
	_signature = params['signature']
	publicKey = PublicKey.find _party_id
	verify = `python3 ../ThresholdCryptography/main.py verifyCertificate #{publicKey["first"]} #{publicKey["second"]} #{params['votes']} #{_signature}`

	if verify == "true"
		  for index in 0 ... params['votes'].size
			create = Votes.create(vote_value: params['votes'].fetch(index)['vote_value'],
				                  ballot_box: params['ballot_box'].to_i,
				                  serial_number: params['serial_number'].to_i,
				                  race_id: index+1)
			end

	else
		complaint = Complaint.create( content: "Failed to verify votes for ballot_box: #{_party_id}, signature: #{_signature}, message: #{params['votes']}" )
		raise
	end

end
  


get '/getBBVotes/:race_id' do |n|
    
  if n.to_i != -1
    votes_hash = Votes.where(race_id: n)
  else
  	votes_hash = Votes.all
  end
  
  	votes_hash.to_json(:except => :updated_at)
end


post '/publishZKP' do
	begin

		_signature = params['signature']
		_vote_id = params['vote_id'].to_i
		_race_id = params['race_id']
		_party_id = params['party_id'].to_i
		_zkp = params['zkp']


	publicKey = PublicKey.find _party_id
	verify = `python3 ../ThresholdCryptography/main.py verifyCertificate #{publicKey["first"]} #{publicKey["second"]} #{_zkp} #{_signature}`

  
	if verify == "true"
	
		voteZkp = ZKP.find_by!(vote_id: _vote_id, race_id: _race_id, party_id: _party_id)
		raise "ZKP already exists for vote_id #{_vote_id}, race_id #{_race_id}, party_id #{_party_id}, zkp: #{_zkp}"

	else
		complaint = Complaint.create( content: "Failed to verify zkp for Party: #{_party_id}, signature: #{_signature}, message: #{_zkp}")
		raise
	end

	rescue ActiveRecord::RecordNotFound
		newZkp = ZKP.create(vote_id: _vote_id, party_id: _party_id, race_id: _race_id, zkp: _zkp)
	end
  
end

get '/getZKP' do
    
  	ZKP.all.to_json(:except => :id)
  
end
