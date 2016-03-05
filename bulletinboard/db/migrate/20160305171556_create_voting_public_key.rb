class CreateVotingPublicKey < ActiveRecord::Migration
  def change
		create_table :VotingPublicKeys, id: false do |t|
    	    t.binary :content
	    end
  end
end
